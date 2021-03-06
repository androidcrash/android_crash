
import json
import math
import glob
import os
import numpy
from scipy.cluster.hierarchy import linkage, dendrogram, fcluster, fclusterdata
from multiprocessing import Pool
from multiprocessing import Pool as ThreadPool
import threadpool
import difflib

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

def string_similar(s1,s2,fuzzing):
    if fuzzing == True:
        return difflib.SequenceMatcher(None, s1, s2).quick_ratio()
    else:
        return (s1 == s2)

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
            if string_similar(stack1[i - 1].symbol,stack2[j - 1].symbol,True) >0.8:
                x = (math.e ** (-c * min(i-1, j-1))) * (math.e ** (-o * abs(i-j)))
            else:
                x = 0.
            M[i][j] = max(M[i-1][j-1] + x, M[i-1][j], M[i][j-1])
    sig = 0.
    for i in range(min(stack_len1, stack_len2)):
        sig += math.e ** (-c * i)

    sim = M[stack_len1][stack_len2] / sig
    return 1 - sim

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

def similar_stack(all_stack, cal, org, dist):
    return clustering(all_stack, cal, org, dist)



def main():
    stack_json_dir = 'data'
    bucket_json = 'data/bucket.json'
    json_file_list = glob.glob(stack_json_dir + os.sep + "df*.json")
    all_stack = []
    for json_file in json_file_list:
        stack = load_stack_stack(json_file)
        all_stack += stack
    print "Clustering"
    new_buckets = similar_stack(all_stack, 0.04, 0.12, 0.1)


if __name__ == "__main__":
    main()

