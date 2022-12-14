import sys 

if(len(sys.argv) != 5):
    print("Incorrect arguments")
    print()
    print("Tool usage: python3 lattice_validater.py <config-path> <output-path> <number-of-processes> <number-of-proposal>")
    print("The config format should be lattice-agreement-<id>.config")
    print("The output format should be <id>.output")
    
    exit()

config_path = sys.argv[1]
if config_path[-1] != '/':
    config_path += '/'

output_path = sys.argv[2]
if output_path[-1] != '/':
    output_path += '/'

number_processes = int(sys.argv[3])
number_proposal = int(sys.argv[4])

# print("Number of processes: " + str(number_processes))
# print("Number of proposals: " + str(number_proposal))

proposes = []
for i in range(number_proposal):
    proposes.append([])

for i in range(1, number_processes+1):
    try:
        with open(config_path + 'lattice-agreement-' + str(i) + '.config') as f:
            # print("Reading: " + config_path + 'lattice-agreement-' + str(i) + '.config')
            lines = f.readlines()
            lines = lines[1:]
            
            p = 0
            for l in lines:
                num_set = set()
                new_l = l.strip().split(' ')
                for n in new_l:
                    num_set.add(int(n))
                proposes[p].append(num_set)
                p+=1
    except:
        print("File not found: " + config_path + 'lattice-agreement-' + str(i) + '.config')


decisions = []
for i in range(number_proposal):
    decisions.append([])

for i in range(1, number_processes+1):
    try:
        with open(output_path + str(i) + '.output') as f:
            # print("Reading: " + output_path + str(i) + '.output')
            lines = f.readlines()

            p=0
            for l in lines:
                num_set = set()
                new_l = l.strip().split(' ')
                for n in new_l:
                    num_set.add(int(n))
                decisions[p].append(list(num_set))
                p+=1

            
            for j in range(p, number_proposal):
                decisions[j].append([])

    except EnvironmentError:
        print(EnvironmentError)
        print("File not found: " + output_path + str(i) + '.output')
    

# validity check
for i in range(number_proposal):
    # create the set of all the proposed values
    all_proposed = set()
    for j in proposes[i]:
        for elem in j:
            all_proposed.add(elem)
    
    for j in range(len(decisions[i])):
        # check that the decision_i contains proposed_i
        # print(decisions[i][j])
        if(len(decisions[i][j]) == 0):
            continue
        for elem in proposes[i][j]:
            if elem not in decisions[i][j]:
                print("Validation failed for proposal " + str(i) + " of process " + str(j) + " since proposed_i is not in decided_i: ", elem)
                print("Proposed_i: ", proposes[i][j])
                print("Decided_i: ", decisions[i][j])
                exit()

        # check that decision_i is subset of all_proposed
        for elem in decisions[i][j]:
            if elem not in all_proposed:
                print("Validation failed for proposal " + str(i) + " of process " + str(j) + " since the decision is not in the set of all proposal: ", elem)
                print("Size of all proposed: ", len(all_proposed))
                print(all_proposed)
                print("")
                exit()

# consistency check
for i in range(len(decisions)):
    for j in range(len(decisions[i])):
        for k in range(j+1, len(decisions[i])):
            if not (set(decisions[i][j]).issubset(decisions[i][k]) or set(decisions[i][k]).issubset(decisions[i][j])):
                print(f"Decision {i} of process {j} and process {k} are not consistent")
                exit()
                
print("Validation is successful!")
         