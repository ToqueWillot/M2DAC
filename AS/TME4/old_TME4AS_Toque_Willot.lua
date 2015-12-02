--TME 4 AS - Auto Encodeurs

--binome Florian Toqué et Paul Willot


require 'nn'
require 'image'
mnist = require 'mnist'

-- Apprentissage par descente de gradient
function fit(mlp, criterion, data, labels, lr, nIter)
   local lr = lr or 1e-2
   local nIter = nIter or 1000
  --  local choices = torch.LongTensor((#data)[1])

   for i = 1,nIter do
      mlp:zeroGradParameters()
      --on shufflise les datas
      -- choices:random((#data)[1])
      -- local x = data:index(1,choices)
      -- local y = labels:index(1,choices)
      indiceRand=math.random((#data)[1])
      local x = data[indiceRand]
      local y = labels[indiceRand]

      --calcul du y chapeau (prediction)
      local pred = mlp:forward(x)
      --calcul de l'erreur entre pred et y*
      local loss = criterion:forward(pred,y)
      --gradient de l'erreur par rapport à pred
      local df_do = criterion:backward(pred,y)
      --propagation du gradient de l'erreur sur  la fonction
      -- linear d'entrée
      local df_di = mlp:backward(x, df_do)
      -- on modifie les poids suivant le learning rate sur lensemble des couches
      mlp:updateParameters(lr)
      if i % 1000 == 0 then
	        print(i,loss)
      end
   end
end



----------------------------------------------------------------------

--fonction qui resize la matrice en la divisant par 2 28*28=>14*14
-- et qui divise tous les "pixel" par 256 pour avoir des valeurs
-- comprisent entre 0 et 1
function resizeMat(mat)
  new_mat=torch.Tensor(mat:size(1)/2,mat:size(2)/2)
  for i=0,mat:size(1)-1,2 do
    for j=0,mat:size(2)-1,2 do
      new_mat[i/2+1][j/2+1] = mat[i+1][j+1]
    end
  end
  return new_mat * (1.0/256)
end


function makeXY(dataset,class, taille)
   taille = taille or 0
   if taille == 0  then
      if dataset == trainset then
	 taille = 11769
      else
	 taille = 1932
      end
   end
   x = torch.ones(taille,14*14) -- taille exemple, 14*14 dimensions
   y = torch.ones(taille)    -- taille exemple
   i=1
   j=1
   while i <= taille  do
      tmp_y = dataset[j].y
      for it=1 ,#class do
      	 if tmp_y == class[it] then
      	    x[i] = resizeMat(dataset[j].x)
      	    y[i]= class[it] + 1
      	    i = i+1
      	 end
      end
      j=j+1
   end
   return x,y
end


-- Données mnist
trainset = mnist.traindataset()
testset = mnist.testdataset()


nEx_train = 2500
nEx_test = 500

class = {0,1,2,3,4,5,6,7,8,9}
nClass= #class


--creation des ensembles testsData et trainData

testsData, testsLabels = makeXY(testset, class, nEx_test)
trainData, trainLabels = makeXY(trainset, class, nEx_train)

----------------------------------------------------------------------
function autoEncoderFit(encoder,decoder,x, criterion,lr,nIter)
  autoEncoder = nn.Sequential()
  autoEncoder:add(encoder)
  autoEncoder:add(nn.Tanh())
  autoEncoder:add(decoder)
  autoEncoder:add(nn.Tanh())
  fit(autoEncoder,criterion, x, x,lr,nIter)
  return encoder
end

--Liste des tailles des différents layers
-- layerSize = {(#trainData)[2],
-- 	     150,
-- 	     100,
-- 	     60,
-- 	     30,
-- 	     20
--              }

layerSize = {(#trainData)[2],
                    65,
                    21,
                    7
            }

 -- layerSize = {(#trainData)[2],
 -- 	     150,
 -- 	     200,
 -- 	     300,
 -- 	     400,
 -- 	     1000
 --              }

-- Entrainement des AutoEncodeurs et stacking des encoders
-- moindre carré loss
mse = nn.MSECriterion()

-- création de lencoder entier "deepEncoder"
deepEncoder = nn.Sequential()
for i=1,(#layerSize)-1 do
   print("AutoEncodeur numéro  ", i)
   x = deepEncoder:forward(trainData)
   -- on entraine  l' autoencodeur i avec x contenant la
   -- (derniere) sortie du forward des encoders précédents
   encoder=nn.Linear(layerSize[i],layerSize[i+1])
   decoder=nn.Linear(layerSize[i+1],layerSize[i])
   encoderFIT=autoEncoderFit(encoder,decoder,x,mse,1e-1,x:size(1))
   -- on ajoute dans le deepencoder l'encoder à présent fitté plus
   -- un nn tanh
   deepEncoder:add(encoderFIT)
   deepEncoder:add(nn.Tanh())
end


-----------------------------------------------------------------------
-----------------------------------------------------------------------

-- Entrainement du classifieur lineaire (sortie dernier encodeur vers
-- prediction de label)
print("Clf:")
classifier = nn.Linear(layerSize[#layerSize], nClass)
-- CrossEntropyCriterion combine nll(negativeloglikelihood et
-- logsoftmax) criterion multiclasse
cec = nn.CrossEntropyCriterion()
--forward du traindata sur le deepEncoder puis fit du classifier sur
-- cette sortie
x = deepEncoder:forward(trainData)
fit(classifier, cec, x, trainLabels, 1e-4, (#x)[1]*1000)

--Consitution du classifieur final
deepClassifier = nn.Sequential()
deepClassifier:add(deepEncoder)
deepClassifier:add(classifier)

pred = deepClassifier:forward(trainData)
__, pred = torch.max(pred,2)
print("score sur data train avant finetuning :")
print(torch.add(trainLabels:long(),-pred):eq(0):double():mean())


--FineTuning
--A present que l'on a ajusté les poids de chaque layer on fit
-- l'ensemble du réseau.
print("Fine tuning...en cours")
fit(deepClassifier, cec, trainData, trainLabels, 1e-2, x:size(1)*30)

-- Evaluation en train
pred = deepClassifier:forward(trainData)
__, pred = torch.max(pred,2)


print("score sur data train:")
print(torch.add(trainLabels:long(),-pred):eq(0):double():mean())


-- for i=1, 10 do
--   print(trainLabels[i]," ----- ",pred[i])
-- end


-- Evaluation en test
pred = deepClassifier:forward(testsData)
__, pred = torch.max(pred,2)
print("score sur data test:")
print(torch.add(testsLabels:long(),-pred):eq(0):double():mean())

-- for i=1, 10 do
--   print(trainLabels[i]," ----- ",pred[i])
-- end
