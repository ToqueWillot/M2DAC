
# coding: utf-8

# # Script

# In[1]:

# from __future__ import exam_success
from __future__ import absolute_import 
from __future__ import print_function  

# Standard imports
get_ipython().magic(u'matplotlib inline')
import os
import sklearn
import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np
import random
import pandas as pd
import scipy.stats as stats

# Sk cheats
from sklearn.cross_validation import cross_val_score
from sklearn import grid_search
from sklearn.ensemble import RandomForestRegressor
from sklearn.ensemble import ExtraTreesRegressor
from sklearn.ensemble import GradientBoostingRegressor
from sklearn.neighbors import KNeighborsRegressor
from sklearn.svm import SVR
#from sklearn.preprocessing import Imputer   # get rid of nan
from sklearn.decomposition import NMF        # to add features based on the latent representation
from sklearn.decomposition import ProjectedGradientNMF

# Faster gradient boosting
import xgboost as xgb

# For neural networks models
from keras.models import Sequential
from keras.layers.core import Dense, Dropout, Activation 
from keras.optimizers import SGD, RMSprop


# In[2]:

print("STARTED...")


# In[3]:

get_ipython().run_cell_magic(u'time', u'', u'#filename = "data/train.csv"\nfilename = "data/reduced_train_10000.csv"\nraw = pd.read_csv(filename)\nraw = raw.set_index(\'Id\')')


# In[4]:

print("LOADED")


# In[5]:

# Considering that the gauge may concentrate the rainfall, we set the cap to 1000
# Comment this line to analyse the complete dataset 
l = len(raw)
raw = raw[raw['Expected'] < 300]  #1000
print("Dropped %d (%0.2f%%)"%(l-len(raw),(l-len(raw))/float(l)*100))


# In[6]:

# We select all features except for the minutes past,
# because we ignore the time repartition of the sequence for now

features_columns = list([u'Ref', u'Ref_5x5_10th',
       u'Ref_5x5_50th', u'Ref_5x5_90th', u'RefComposite',
       u'RefComposite_5x5_10th', u'RefComposite_5x5_50th',
       u'RefComposite_5x5_90th', u'RhoHV', u'RhoHV_5x5_10th',
       u'RhoHV_5x5_50th', u'RhoHV_5x5_90th', u'Zdr', u'Zdr_5x5_10th',
       u'Zdr_5x5_50th', u'Zdr_5x5_90th', u'Kdp', u'Kdp_5x5_10th',
       u'Kdp_5x5_50th', u'Kdp_5x5_90th'])

def getXy(raw):
    selected_columns = list([ u'minutes_past',u'radardist_km', u'Ref', u'Ref_5x5_10th',
       u'Ref_5x5_50th', u'Ref_5x5_90th', u'RefComposite',
       u'RefComposite_5x5_10th', u'RefComposite_5x5_50th',
       u'RefComposite_5x5_90th', u'RhoHV', u'RhoHV_5x5_10th',
       u'RhoHV_5x5_50th', u'RhoHV_5x5_90th', u'Zdr', u'Zdr_5x5_10th',
       u'Zdr_5x5_50th', u'Zdr_5x5_90th', u'Kdp', u'Kdp_5x5_10th',
       u'Kdp_5x5_50th', u'Kdp_5x5_90th'])
    
    data = raw[selected_columns]
    
    docX, docY = [], []
    for i in data.index.unique():
        if isinstance(data.loc[i],pd.core.series.Series):
            m = [data.loc[i].as_matrix()]
            docX.append(m)
            docY.append(float(raw.loc[i]["Expected"]))
        else:
            m = data.loc[i].as_matrix()
            docX.append(m)
            docY.append(float(raw.loc[i][:1]["Expected"]))
    X , y = np.array(docX) , np.array(docY)
    return X,y


# In[7]:

noFullNan = raw.loc[raw[features_columns].dropna(how='all').index.unique()]


# In[8]:

X,y=getXy(noFullNan)


# In[9]:

global_means = np.nanmean(noFullNan,0)

XX=[]
for t in X:
    nm = np.nanmean(t,0)
    for idx,j in enumerate(nm):
        if np.isnan(j):
            nm[idx]=global_means[idx]
    XX.append(nm)
XX=np.array(XX)

# rescale to clip min at 0 (for non negative matrix factorization)
XX_rescaled=XX[:,:]-np.min(XX,0)


# In[10]:


nmf = NMF(max_iter=5000)
W = nmf.fit_transform(XX_rescaled)
#H = nn.components_


# In[11]:

# reduce the sequence structure of the data and produce
# new hopefully informatives features
def addFeatures(X,mf=0):
    # used to fill fully empty datas
    #global_means = np.nanmean(X,0)
    
    XX=[]
    nbFeatures=float(len(X[0][0]))
    for idxt,t in enumerate(X):
        
        # compute means, ignoring nan when possible, marking it when fully filled with nan
        nm = np.nanmean(t,0)
        tt=[]
        for idx,j in enumerate(nm):
            if np.isnan(j):
                nm[idx]=global_means[idx]
                tt.append(1)
            else:
                tt.append(0)
        tmp = np.append(nm,np.append(tt,tt.count(0)/nbFeatures))
        
        # faster if working on fully filled data:
        #tmp = np.append(np.nanmean(np.array(t),0),(np.array(t)[1:] - np.array(t)[:-1]).sum(0) )
        
        # add the percentiles
        tmp = np.append(tmp,np.nanpercentile(t,10,axis=0))
        tmp = np.append(tmp,np.nanpercentile(t,50,axis=0))
        tmp = np.append(tmp,np.nanpercentile(t,90,axis=0))
        
        for idx,i in enumerate(tmp):
            if np.isnan(i):
                tmp[idx]=0

        # adding the dbz as a feature
        test = t
        try:
            taa=test[:,0]
        except TypeError:
            taa=[test[0][0]]
        valid_time = np.zeros_like(taa)
        valid_time[0] = taa[0]
        for n in xrange(1,len(taa)):
            valid_time[n] = taa[n] - taa[n-1]
        valid_time[-1] = valid_time[-1] + 60 - np.sum(valid_time)
        valid_time = valid_time / 60.0


        sum=0
        try:
            column_ref=test[:,2]
        except TypeError:
            column_ref=[test[0][2]]
        for dbz, hours in zip(column_ref, valid_time):
            # See: https://en.wikipedia.org/wiki/DBZ_(meteorology)
            if np.isfinite(dbz):
                mmperhr = pow(pow(10, dbz/10)/200, 0.625)
                sum = sum + mmperhr * hours
        
        if not(mf is 0):
            tmp = np.append(tmp,mf[idxt])

        XX.append(np.append(np.array(sum),tmp))
        #XX.append(np.array([sum]))
        #XX.append(tmp)
    return np.array(XX)


# In[12]:


XX=addFeatures(X,mf=W)


# In[13]:

print("FEATURES EXTRACTED")


# ---

# In[14]:

etreg = ExtraTreesRegressor(n_estimators=200, max_depth=None, min_samples_split=1, n_jobs=8)
etreg = etreg.fit(XX,y)


# In[15]:

print("MODEL 1 TRAINED")


# In[16]:

etreg2 = ExtraTreesRegressor(n_estimators=200, max_depth=None, min_samples_split=1, n_jobs=8)
etreg2 = etreg.fit(XX,y)


# In[17]:

print("MODEL 2 TRAINED")


# In[18]:

etreg3 = ExtraTreesRegressor(n_estimators=200, max_depth=None, min_samples_split=1, n_jobs=8)
etreg3 = etreg.fit(XX,y)


# In[19]:

print("MODEL 3 TRAINED")


# In[20]:

#knn,etreg,rfr,xgbr,gbr


# # Predict on testset

# In[21]:

#filename = "data/test.csv"
filename = "data/reduced_test_5000.csv"
test = pd.read_csv(filename)
test = test.set_index('Id')


# In[22]:

print("TEST LOADED")


# In[23]:

features_columns = list([u'Ref', u'Ref_5x5_10th',
       u'Ref_5x5_50th', u'Ref_5x5_90th', u'RefComposite',
       u'RefComposite_5x5_10th', u'RefComposite_5x5_50th',
       u'RefComposite_5x5_90th', u'RhoHV', u'RhoHV_5x5_10th',
       u'RhoHV_5x5_50th', u'RhoHV_5x5_90th', u'Zdr', u'Zdr_5x5_10th',
       u'Zdr_5x5_50th', u'Zdr_5x5_90th', u'Kdp', u'Kdp_5x5_10th',
       u'Kdp_5x5_50th', u'Kdp_5x5_90th'])

def getX(raw):
    selected_columns = list([ u'minutes_past',u'radardist_km', u'Ref', u'Ref_5x5_10th',
       u'Ref_5x5_50th', u'Ref_5x5_90th', u'RefComposite',
       u'RefComposite_5x5_10th', u'RefComposite_5x5_50th',
       u'RefComposite_5x5_90th', u'RhoHV', u'RhoHV_5x5_10th',
       u'RhoHV_5x5_50th', u'RhoHV_5x5_90th', u'Zdr', u'Zdr_5x5_10th',
       u'Zdr_5x5_50th', u'Zdr_5x5_90th', u'Kdp', u'Kdp_5x5_10th',
       u'Kdp_5x5_50th', u'Kdp_5x5_90th'])
    
    data = raw[selected_columns]
    
    docX= []
    for i in data.index.unique():
        if isinstance(data.loc[i],pd.core.series.Series):
            m = [data.loc[i].as_matrix()]
            docX.append(m)
        else:
            m = data.loc[i].as_matrix()
            docX.append(m)
    X = np.array(docX)
    return X


# In[24]:

#%%time
#X=getX(test)

#tmp = []
#for i in X:
#    tmp.append(len(i))
#tmp = np.array(tmp)
#sns.countplot(tmp,order=range(tmp.min(),tmp.max()+1))
#plt.title("Number of ID per number of observations\n(On test dataset)")
#plt.plot()


# In[25]:

#testFull = test.dropna()
testNoFullNan = test.loc[test[features_columns].dropna(how='all').index.unique()]


# In[26]:

test_X=getX(testNoFullNan)  # 1min
#XX = [np.array(t).mean(0) for t in X]  # 10s


# In[27]:

test_XX=[]
for t in test_X:
    nm = np.nanmean(t,0)
    for idx,j in enumerate(nm):
        if np.isnan(j):
            nm[idx]=global_means[idx]
    test_XX.append(nm)
test_XX=np.array(test_XX)

# rescale to clip min at 0 (for non negative matrix factorization)
test_XX_rescaled=test_XX[:,:]-np.min(test_XX,0)


# In[28]:

test_W = nmf.transform(test_XX_rescaled)


# In[29]:

test_XX=addFeatures(test_X,mf=test_W)


# In[30]:

print("PREDICTING...")


# In[31]:

reducedModelList = [etreg,etreg2,etreg3]
test_globalPred = np.array([f.predict(test_XX) for f in reducedModelList]).T
predTest = test_globalPred.mean(1)


# In[32]:

predFull = zip(testNoFullNan.index.unique(),predTest)


# In[33]:

testNan = test.drop(test[features_columns].dropna(how='all').index)


# In[34]:

tmp = np.empty(len(testNan))
tmp.fill(0.445000)   # 50th percentile of full Nan dataset
predNan = zip(testNan.index.unique(),tmp)


# In[35]:

pred = predFull + predNan


# In[36]:

pred.sort(key=lambda x: x[0], reverse=False)


# In[37]:

submission = pd.DataFrame(pred)
submission.columns = ["Id","Expected"]
submission.head()


# In[38]:

print("DUMPING")


# In[39]:

submission.loc[submission['Expected']<0,'Expected'] = 0.01


# In[40]:

submission.to_csv("submit6.csv",index=False)


# In[41]:

print("OVER")


# In[ ]:



