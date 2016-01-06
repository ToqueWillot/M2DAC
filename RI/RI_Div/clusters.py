# -*- coding: utf-8 -*-

import random
import numpy as np
from scipy import stats
from sklearn.cluster import KMeans
from sklearn.decomposition import PCA

# Fichier contenant la représentation vectoriel de tous les documents
data_file = 'data'
# Fichier contenant le ranking sans diversité
# Ranking effectué avec un modèle vectoriel
baseline_file = 'baseline'
# Fichier générer par notre moteur de recherche, permet de récupérer les identifiants des documents
dico_file = 'easyCLEF08_text_index'
# Fichiers contenant toutes les requêtes avec les documents pertinents asssociés
rel_file = 'relevants'

### Lecture des fichiers

def get_data(n, m, path):
    X = np.zeros((n, m))
    Y = np.zeros(n)
    i = 0
    for line in open(path):
        tmp = line.split()
        j = 0
        for x in tmp[:-1]: # y en fin
            X[i][j] = float(x)
            j+= 1
        Y[i] = tmp[-1]
        i += 1
        if i == n:
            break
    return X, Y

def get_relevants(dico, path):
    Q = {}
    f = open(path)
    for line in open(path):
        tmp = line.split()
        n = len(tmp[:-1])
        print "nbRel Query id " + tmp[-1] + " = " + str(n)
        aux = []
        for i in range(n):
            aux.append(dico[tmp[i]])
        Q[tmp[-1]] = np.array(aux)
    return Q

def get_ranking_baseline(dico, path):
    Q = {}
    for line in open(path):
        tmp = line.split()
        n = len(tmp[:-1])
        aux = []
        for i in range(n):
            aux.append(dico[tmp[i]])
        Q[tmp[-1]] = np.array(aux)
    return Q

def get_dico(path):
    dico = {}
    cpt = 0
    for line in open(path):
        tmp = line.split(':')
        dico[tmp[0]] = cpt
        cpt += 1
    return dico



### PARAMETERS

# n est le nombre de documents à renvoyer
n = 20
print "n = " + str(n)
# Sur combien d'éxécutions on moyenne
mean = 3
nbClusters = 18 # doit être plus petit que 'n', et plus petit que le 'nb_topics'
debug = False

nbrDoc_baseline = 200 # doit être plus grand que 184 (nbRel max des queries)




# On a 2256 documents, chaque document est représenter par un vecteur de taille 2353,
# avec comme dernière valeur sont topic
X, Y = get_data(2256, 2354, data_file)
nb_topics = len(np.unique(Y))
print "Nombre topics (tous documents) : " + str(nb_topics)

dico = get_dico(dico_file)
baseline = get_ranking_baseline(dico, baseline_file)
relevants = get_relevants(dico, rel_file)

# Tous les ranking sont constitués des indices des documents dans X

### Algorithmes de diversité

# Sélectionne 'n' documents aléatoirement dans la baseline
def ranking_div_alea(baseline, n):
    return random.sample(xrange(baseline.size), n)

# Sélection aléatoire 'n' documents en alternant les clusters
def ranking_div_kmeans(baseline, n, nbClusters):
    #reduced_data = PCA(n_components=10).fit_transform(X)
    kmeans = KMeans(init='k-means++', n_clusters=nbClusters)
    #kmeans.fit(reduced_data)
    kmeans.fit(X[baseline])

    # on attribue a chaque cluster l'indice du documents X
    matching = {}
    cpt = 0
    for c in kmeans.labels_:
        if matching.has_key(c):
            matching[c].append(baseline[cpt])
        else:
            matching[c] = [baseline[cpt]]
        cpt += 1

    #print "clusters : " +  str(kmeans.cluster_centers_.shape)

    res = []
    nlabels = kmeans.n_clusters
    tab_nsamples = np.zeros(nlabels)
    i = 0
    while True:
        for k in range(nlabels):
            if len(matching[k]) > tab_nsamples[k]:
                tab_nsamples[k] += 1
                i += 1
            if i >= n:
                break
        if i >= n:
            break
    for k in range(nlabels):
        res += random.sample(np.array(matching[k]), int(tab_nsamples[k]))
    return np.array(res)

# Sélection 'n' documents dans les clusters en conservant le ranking de baseline
# cad la similarité à la requête
# Les clusters sont parcourus par rapport à la distance de leurs centres à la requête
def ranking_div_kmeans2(baseline, n, nbClusters):
    #reduced_data = PCA(n_components=500).fit_transform(X)
    kmeans = KMeans(init='k-means++', n_clusters=nbClusters)
    #kmeans.fit(reduced_data)
    kmeans.fit(X[baseline])

    # on attribue a chaque cluster l'indice du documents X
    matching = {}
    cpt = 0
    for c in kmeans.labels_:
        if matching.has_key(c):
            matching[c].append(baseline[cpt])
        else:
            matching[c] = [baseline[cpt]]
        cpt += 1

    #print "clusters : " +  str(kmeans.cluster_centers_.shape)

    res = []
    nlabels = kmeans.n_clusters
    tab_nsamples = np.zeros(nlabels)
    i = 0

    ordo_cluster = []

    req = X[baseline[0]]
    dist = np.array([((c - req)**2).sum() for c in kmeans.cluster_centers_])
    dist = np.argsort(dist)

    while True:
        for k in dist:
            if len(matching[k]) > tab_nsamples[k]: # FIXME : attention !
                tab_nsamples[k] += 1
                i += 1
            if i >= n:
                break
        if i >= n:
            break
    for k in range(nlabels):
        res += matching[k][:int(tab_nsamples[k])]
    return np.array(res)


def p_n(ranking, rel, n):
    tmp = ranking[:n]
    return sum(np.array([x in rel for x in tmp])) / float(n)

def cr_n(ranking, rel, n):
    nb_topics_q = len(np.unique(Y[rel]))
    #print "Nombre sous thème de la query : " + str(nb_topics_q)
    return (len(np.unique(Y[ranking[:n]]))) / float(nb_topics_q)







# Exécution

cr_mean = 0
p_mean = 0
for q in relevants.keys():
    print "#### Query " + q
    acc1 = 0
    acc2 = 0
    #print "nombre doc dans la baseline : " + str(baseline[q].shape[0])
    for i in range(mean):
        rel = relevants[q]
        ranking = baseline[q][:nbrDoc_baseline]
        #ranking = ranking_div_alea(baseline[q][:nbrDoc_baseline], n)
        #ranking = ranking_div_kmeans2(baseline[q][:nbrDoc_baseline], n, nbClusters)
        acc1 += cr_n(ranking, rel, n)
        acc2 += p_n(ranking, rel, n)
    print "CR = " + str(acc1 / float(mean)) + "  P = " + str(acc2 / float(mean))
    cr_mean += acc1 / float(mean)
    p_mean += acc2 / float(mean)
    if debug:
        break
print "\n===> CR Query Mean = " + str(cr_mean / len(relevants.keys())) + ", P Query Mean = " + str(p_mean / len(relevants.keys()))
