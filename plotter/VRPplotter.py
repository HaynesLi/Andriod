import os
import matplotlib.pyplot as plt
import numpy as np
import re

def readline(f):
    return f.readline()

def read_int(f):
    return int(readline(f))

def read_integers(f):
    return list(map(int,readline(f).split()))

def read_floats(f):
    return list(map(float,readline(f).split()))

def readProblemInstance(filename):
    with open(filename, "r") as f:
        K, n = read_integers(f)
        locations = []
        for _ in range(n):
            locations.append(tuple(read_integers(f)))
        return n, K, locations

def readSolution(filename, K):
    with open(filename, "r") as f:
        solutions = []
        f.readline()
        costline = f.readline()
        f.readline()

        costStr = costline.split(" ")[1]
        cost = float(costStr.replace('.','').replace(',','.'))
        for _ in range(K):
            solutions.append(read_integers(f))
        return solutions, cost

def createPlot(inputfile, solutionfile, problemID):
    n, K, locations = readProblemInstance(inputfile)
    routes, cost = readSolution(solutionfile, K)

    colors = [np.random.rand(3) for _ in range(K)]
    for t, c in zip(routes, colors):
        r = t
        while len(r) > 1:
            a, b, r = r[0], r[1], r[1:]
            p1, p2 = locations[a], locations[b]
            plt.plot([p1[0], p2[0]], [p1[1], p2[1]], color=c)

    # draw the map again
    for p in locations:
        plt.plot(p[0], p[1], '.', color='green')

    plt.plot(locations[0][0], locations[0][1], 'o', color='red')

    plt.title("" + str(K) + (' Vehicles' if K > 1 else ' Vehicle') + " - " + str(n) + " Locations - Cost: " + str(cost))
    plotname = "plot_"+problemID+".png"

    folderpath = makePath("plots/")
    if not os.path.exists(folderpath):
        os.makedirs(folderpath)

    plt.savefig(folderpath + plotname)
    plt.close()
    print("Plot '{}' created!".format(plotname))

overwritePlots = False
def handleInputFile(inputfile):
    problemID = re.search('input_(.*).txt', inputfile).group(1)
    solutionfile = makePath("output/solution_{}.txt").format(problemID)
    plot = makePath("plots/plot_{}.png").format(problemID)
    if os.path.isfile(solutionfile) and (overwritePlots or not os.path.isfile(plot)):
        createPlot(makePath("input/" + inputfile), solutionfile, problemID)

def makePath(path):
    return EXP_HOME + path

if __name__ == "__main__":
    EXP_HOME = os.environ.get("EXPERIMENTS_HOME", None)
    if EXP_HOME is None:
        print("Please set the EXPERIMENTS_HOME environment variable")
        exit(1)
    for root, dirs, files in os.walk(EXP_HOME + "input"):
        for file in files:
            if file.startswith("input_") and file.endswith(".txt"):
                handleInputFile(file)