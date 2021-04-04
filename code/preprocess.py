import re
import json
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


class StackTraceExtractor:
    def __init__(self):
        self.JAVA_TRACE = r'\s*?at\s+([\w<>\$_]+\.)+([\w<>\$_]+)\s*\((.+?)\.java:?(-\d+|\d+)?\)'
        self.JAVA_EXCEPTION = r'\n(([\w<>\$_]++\.?)++[\w<>\$_]*+(Exception|Error){1}(\s|:))'
        self.JAVA_CAUSE = r'(Caused by:).*?(Exception|Error)(.*?)(\s+at.*?\(.*?:\d+\))+'
        self.RE_FLAGS = re.I | re.M | re.S

    def find_stack_traces(self, s):
        stack_traces = []

        for r in re.findall(re.compile(self.JAVA_TRACE, self.RE_FLAGS), s):
            if "Native Method" not in r[2]:
                item = (r[0] + r[1], r[2] + ":" + r[3])
                if item not in stack_traces:
                    stack_traces.append(item)

        return stack_traces

def load_stacks(stack_csv):
    with open(stack_csv) as f:
        lines = f.readlines()
    stackTraceExtractor = StackTraceExtractor()
    stacks = []
    for line in lines:
        issue_id = line.split(',')[0]
        duplicated_issue = line.split(',')[3]
        duplicates_id = None
        description = line.split(',')[5]
        ori_frames = stackTraceExtractor.find_stack_traces(description)
        frames = []
        if len(ori_frames) is 0:
            continue
        if len(duplicated_issue) is not 0:
            duplicates_ids = duplicated_issue.split('.')
            dps = []
            for dp_id in duplicates_ids:
                if dp_id == '0':
                    continue
                dps.append(dp_id)
            duplicates_id = ','.join(dps)
            if len(duplicates_ids) > 2:
                print duplicates_id
                exit(0)
            
        for ori_frame in ori_frames:
            frame_dict = dict()
            frame_dict['symbol'] = ori_frame[0].strip()
            frame_dict['file'] = ori_frame[1].split(':')[0]
            frame = Frame(frame_dict)
            frames.append(frame)
        stack = Stack(issue_id, frames, duplicates_id)
        stacks.append(stack)
    return stacks

def save_json(output_json, stacks):
    with open(output_json, 'w') as fb_output:
        output_json_arr = []
        for stack in stacks:
            stack_dict = dict()
            stack_dict['stack_id'] = stack.id
            stack_dict['duplicated_stack'] = stack.duplicated_stack
            stack_dict['stack_arr'] = []
            for frame in stack.stack_arr:
                frame_dict = dict()
                frame_dict['symbol'] = frame.symbol
                frame_dict['file'] = frame.file
                stack_dict['stack_arr'].append(frame_dict)
            output_json_arr.append(stack_dict)
        json.dump(output_json_arr, fb_output)

def compare_stack(stack1, stack2):
    min_len = min([len(stack1.stack_arr),len(stack2.stack_arr)])
    for i in range(0, min_len):
        if stack1.stack_arr[i].symbol != stack2.stack_arr[i].symbol:
            return False
    return True

def same_filter(stacks):
    duplicated_stack_arr = []

    for i, stack in enumerate(stacks):
        for j in range(i + 1, len(stacks)):
            if compare_stack(stack, stacks[j]):
                if stack.duplicated_stack in duplicated_stack_arr:
                    continue
                if len(stack.duplicated_stack) != 0:
                    continue
                duplicated_stack_arr.append(stack.id)
                stacks[j].duplicated_stack = stack.id
    return stacks
        
def preprocess(stacks):

    real_buckets = []

    for stack in stacks:
        found = False
        for bucket in real_buckets:
            if stack.id in bucket and stack.duplicated_stack in bucket:
                found = True
                break
            if stack.id not in bucket and stack.duplicated_stack not in bucket:
                continue
                # real_buckets.append([stack.id])
            if stack.id in bucket:
                for d_stack in stacks:
                    if d_stack.id == stack.duplicated_stack:
                        bucket.append(d_stack.id)
            else:
                bucket.append(stack.id)
            found = True
        if not found:
            real_buckets.append([stack.id])
            for d_stack in stacks:
                if d_stack.id == stack.duplicated_stack:
                    real_buckets[-1].append(d_stack.id)
    return real_buckets
def process_csv():
    input_arr = ['*.csv']
    output_arr = ['*.json']

    for index, input_csv in enumerate(input_arr):
        stacks = load_stacks(input_csv)
        print len(preprocess(stacks))
        stacks = same_filter(stacks)
        print len(preprocess(stacks))
        save_json(output_arr[index], stacks)
        
if __name__ == "__main__":
    process_csv()
