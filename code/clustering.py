
import json
import math
import glob
import os
import numpy
from scipy.cluster.hierarchy import linkage, dendrogram, fcluster, fclusterdata
from multiprocessing import Pool
from multiprocessing import Pool as ThreadPool
import threadpool

STACK = None
BUCKETS = []
res_list = list(numpy.ones(3000))
task_pool = threadpool.ThreadPool(4)

class Stack(object):
    stack_arr = []
    id = ''
    duplicated_stack=''

    def __init__(self, id, frame_arr, duplicated_stack=None):
        self.id = id
        self.stack_arr = frame_arr
        if duplicated_stack is not None:
            self.duplicated_stack = duplicated_stack

    def __len__(self):
        return len(self.stack_arr)

    def __getitem__(self, index):
        return self.stack_arr[index]


class Frame(object):
    def __init__(self, frame, with_str = False):
        if with_str:
            self.symbol = frame
        else:
            self.symbol = frame['symbol']
            self.file = frame['file']

def load_stack_feature(stack_json):
    with open(stack_json) as f:
        apm_dict = json.load(f)
    all_stack = []
    for _hits_item in apm_dict['hits']['hits']:
        if _hits_item['_source']['feature'] is None:
            continue
        frames = _hits_item['_source']['feature'][0]['frame']

        stack_id = _hits_item['_id']
        stack_arr = []
        for frame_dict in frames:
            frame = Frame(frame_dict)
            stack_arr.append(frame)
        stack = Stack(stack_id, stack_arr)
        all_stack.append(stack)
    return all_stack

def load_stack_stack(stack_json):
    with open(stack_json) as f:
        apm_dict = json.load(f)
    all_stack = []
    for _hits_item in apm_dict['hits']['hits']:
        if _hits_item['_source']['stack'] is None:
            continue
        stack_id = _hits_item['_id']
        stacks_str = _hits_item['_source']['stack'][1:-1]
        frames = stacks_str.split(',')

        stack_arr = []
        for frame_str in frames:
            frame = Frame(frame_str, True)
            stack_arr.append(frame)
        stack = Stack(stack_id, stack_arr)
        all_stack.append(stack)
    return all_stack

def load_buckets(bucket_json):
    return []

def get_dist(stack1, stack2, c, o):
    stack_len1 = len(stack1)
    stack_len2 = len(stack2)

    if stack_len1 == 1:
        return 1.0
    if stack_len2 == 1:
        return 1.0
    M = [[0. for i in range(len(stack2) + 1)] for j in range(len(stack1) + 1)]
    
    for i in xrange(1, len(stack1) + 1):
        for j in xrange(1, len(stack2) + 1):
            if stack1[i - 1].symbol == stack2[j - 1].symbol:
                x = (math.e ** (-c * min(i-1, j-1))) * (math.e ** (-o * abs(i-j)))
            else:
                x = 0.
            M[i][j] = max(M[i-1][j-1] + x, M[i-1][j], M[i][j-1])
    sig = 0.
    for i in range(min(stack_len1, stack_len2)):
        sig += math.e ** (-c * i)

    sim = M[stack_len1][stack_len2] / sig
    return 1 - sim

def stack_prefix(stack1, stack2, num):
    for i in range(0, num):
        if i >= len(stack1.stack_arr) or i >= len(stack2.stack_arr):
            return False
        if stack1.stack_arr[i].symbol != stack2.stack_arr[i].symbol:
            return False
    return True
    
def prefix_match(all_stack):
    prefix = 4
    buckets = []
    for stack in all_stack:
        found = False
        for bucket in buckets:
            if stack_prefix(bucket[0], stack, prefix):
                bucket.append(stack)
                found = True
        if not found:
            buckets.append([stack])
    return buckets


def clustering(all_stack, cal, org, dist):
    sim = []
    for i in range(len(all_stack) - 1):
        for j in range(i + 1, len(all_stack)):
            res = get_dist(all_stack[i], all_stack[j], cal, org)
            sim.append(res)
    link = linkage(sim, method = 'complete')
    result = fcluster(link, dist, criterion='distance', depth = 2, R=None, monocrit = None)
    maximum = max(result)
    bucket = [[] for i in range(maximum)]
    for i in range(len(result)):
        bucket[int(result[i]) - 1].append(all_stack[i])
    bucket.sort()
    return bucket

def similar_stack(all_stack, buckets, cal, org, dist):
    return clustering(all_stack, cal, org, dist)

def write_buckets(buckets, bucket_file):
    buckets_array = []
    for bucket in buckets:
        stack_arr = []
        for stack in bucket:
            stack_dict = dict()
            frame_arr = []
            for frame in stack.stack_arr:
                frame_arr.append(frame.symbol)
            stack_dict[stack.id] = frame_arr
            stack_arr.append(stack_dict)
        buckets_array.append(stack_arr)
    buckets_json = json.dumps(buckets_array)
    with open(bucket_file, 'w') as f:
        json.dump(buckets_json, f)

def cal_dist(index):
    cal = 0.05
    org = 0.14
    global STACK
    sim = get_dist(STACK, BUCKETS[index][0], cal, org)
    res_list[index] = sim

def cal_dist_process(stack1, stack2, index):
    cal = 0.05
    org = 0.14
    sim = get_dist(stack1, stack2, cal, org)
    return (index, sim)

def single_pass_clustering_2(stack, c, o, dist):
    global res_list
    for i in range(0, len(res_list)):
        res_list[i] = 1.0

    requests = threadpool.makeRequests(cal_dist, [([index, stack], None) for index in range(0, len(BUCKETS))])
    [task_pool.putRequest(req) for req in requests]
    task_pool.wait()
    if len(BUCKETS) is 0:
        BUCKETS.append([stack])
        return
    min_sim = min(list(res_list))
    if min_sim <= dist:
        min_idx = res_list.index(min_sim)
        BUCKETS[min_idx].append(stack)
    else:
        BUCKETS.append([stack])

def single_pass_clustering_3(stack, c, o, dist):
    global res_list
    for i in range(0, len(res_list)):
        res_list[i] = 1.0
    global STACK
    STACK = stack
    mypool = ThreadPool(8)
    mypool.map(cal_dist,[i for i in range(0,len(BUCKETS))])
    mypool.close()
    mypool.join()

    if len(BUCKETS) is 0:
        BUCKETS.append([stack])
        return
    min_sim = min(list(res_list))
    if min_sim <= dist:
        min_idx = res_list.index(min_sim)
        BUCKETS[min_idx].append(stack)
    else:
        BUCKETS.append([stack])

def single_pass_clustering_4(stack, c, o, dist):
    global res_list
    for i in range(0, len(res_list)):
        res_list[i] = 1.0
    pool = Pool(processes=4)
    result = []
    for i in range(0, len(BUCKETS)):
        result.append(pool.apply_async(cal_dist_process, (stack,BUCKETS[i],i)))
    pool.close()
    pool.join()
    for res in result:
        res_list[res.get()[0]] = res.get()[1]

    if len(BUCKETS) is 0:
        BUCKETS.append([stack])
        return
    min_sim = min(list(res_list))
    if min_sim <= dist:
        min_idx = res_list.index(min_sim)
        BUCKETS[min_idx].append(stack)
    else:
        BUCKETS.append([stack])


def single_pass_clustering(stack, c, o, dist):
    found = False
    min = -1
    min_sim = -1
    for i, bucket in enumerate(BUCKETS):
        sim = get_dist(stack, bucket[0], c, o)
        if sim < min_sim:
            min_sim = sim
            min = i
        if sim < dist:
            bucket.append(stack)
            found = True
            min = i
            min_sim = sim
    if found is not True:
        BUCKETS.append([stack])

def query(stack):
    print "Got"

def main():
    stack_json_dir = 'data'
    bucket_json = 'data/bucket.json'
    json_file_list = glob.glob(stack_json_dir + os.sep + "df*.json")
    all_stack = []
    for json_file in json_file_list:
        stack = load_stack_stack(json_file)
        all_stack += stack
    print "Clustering"
    buckets = load_buckets(bucket_json)
    new_buckets = similar_stack(all_stack, buckets, 0.0, 0.0, 0.1)
    write_buckets(new_buckets, bucket_json)

    print "single_pass_clustering..."
    for stack in all_stack:
        single_pass_clustering(stack, 0.0, 0.0, 0.1)
    print "We have " + str(len(BUCKETS)) + " buckets Now"

    print "prefix_match..."
    prefix_buckets = prefix_match(all_stack)
    print "We have " + str(len(prefix_buckets)) + " buckets Now"

if __name__ == "__main__":
    main()

