--TME 4 AS - Auto Encodeurs

--binome Florian Toqué et Paul Willot


require 'nn'
require 'image'
mnist = require 'mnist'

-- Apprentissage par descente de gradient
function fit(mlp, criterion, data, labels, lr, nIter)
   local lr = lr or 1e-1
   local nIter = nIter or 1000
   local choices = torch.LongTensor((#data)[1])
   for i = 1,nIter do
      mlp:zeroGradParameters()
      --on shufflise les datas
      choices:random((#data)[1])
      local x = data:index(1,choices)
      local y = labels:index(1,choices)
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
      if i % 100 == 0 then
	        print(i,loss)
      end
   end
end

--fonction permettant de creer la suite des decoder à une certaine
-- profondeur depuis decoders (qui les contient tous)
function buildDeepDecoder(decoders, depth)
   local decoder = nn.Sequential()
   for i = depth,1,-1 do
      decoder:add(decoders[i])
      decoder:add(nn.Tanh())
   end
   return decoder
end

--permet de visualiser les features apprises
function visualizeAutoEncoding(deepEncoder, deepDecoder, data)
   local depth =  (#deepEncoder.modules)/2
   local imgSize = math.sqrt((#data)[2])
   for i = 1,(#data)[1] do
      local img = data[i]:reshape(imgSize,imgSize)
      image.save("image_input".. depth .."_" .. i .. ".png", img)
      --decoding sur l'encoding de limage data[i] a la profondeur depth
      img = deepDecoder:forward(deepEncoder:forward(data[i]))
      img = img:reshape(imgSize,imgSize)
      image.save("image_output".. depth .. "_" .. i ..".png", img)
   end
end

function visualizeDecoding(deepDecoder, code)
   local img = deepDecoder:forward(code)
   local imgSize = math.sqrt((#img)[1])
   img = img:reshape(imgSize,imgSize)
   return img
end

----------------------------------------------------------------------
-- Données mnist
trainset = mnist.traindataset()
testset = mnist.testdataset()


classes = torch.range(0,9)
nClass = (#classes)[1]

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

--creation des ensembles testsData et trainData
nEx = testset.size
testsData = torch.zeros(nEx, 14*14)
testsLabels = torch.zeros(nEx)
for i=1,nEx do
   testsLabels[i] = testset.label[i] + 1
   testsData[i] = resizeMat(testset.data[i])
end

nEx = trainset.size
trainData = torch.zeros(nEx, 14*14)
trainLabels = torch.zeros(nEx)
for i=1,nEx do
   trainLabels[i] = trainset.label[i] + 1
   trainData[i] = resizeMat(trainset.data[i])
end

----------------------------------------------------------------------
-- Liste des tailles des différents layers
layerSize = {(#trainData)[2],
	     150,
	     100,
	     60,
	     30,
	     20
             }

-- Constitution des layers encoders et decoders
--un autoencoder est formé comme cela:
-- nn.sequential->nn.linear(encoder)->tanh->linear(decoder)->tanh
encoders = {}
decoders = {}
for i=1,(#layerSize)-1 do
   table.insert(encoders, nn.Linear(layerSize[i],layerSize[i+1]))
   table.insert(decoders, nn.Linear(layerSize[i+1],layerSize[i]))
end

-- Constitution des autoencoders
autoEncoders = {}
for i=1,#encoders do
   autoEncoder = nn.Sequential()
   autoEncoder:add(encoders[i])
   autoEncoder:add(nn.Tanh())
   autoEncoder:add(decoders[i])
   autoEncoder:add(nn.Tanh())
   table.insert(autoEncoders, autoEncoder)
end

-- Entrainement des AutoEncodeurs et stacking des couches
-- moindre carré loss
mse = nn.MSECriterion()
-- création de lencoder entier "deepEncoder"
deepEncoder = nn.Sequential()
for i=1,(#autoEncoders) do
   print("AutoEncodeur numéro  ", i)
   x = deepEncoder:forward(trainData)
   -- on entraine  l' autoencodeur i avec x contenant la
   -- (derniere) sortie du forward des encoders précédents
   fit(autoEncoders[i], mse, x, x, 1e-1, 100)
   -- on ajoute dans le deepencoder l'encoder i à présent fitté plus
   -- un nn tanh
   deepEncoder:add(encoders[i])
   deepEncoder:add(nn.Tanh())
   -- visualization de l'auto encoding avec le deepencoder à la ieme
   -- couche(sur les images 6,7,8,9,10 )
   visualizeAutoEncoding(deepEncoder, buildDeepDecoder(decoders, i), trainData[{{6,10}}])
end

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
fit(classifier, cec, x, trainLabels, 1e-1, 100)

--Consitution du classifieur final
deepClassifier = nn.Sequential()
deepClassifier:add(deepEncoder)
deepClassifier:add(classifier)


--Visualiser un decodage de la dernière couche:
codeSize = layerSize[#layerSize]
for i = 1,codeSize do
   code = torch.zeros(codeSize)
   code[i] = 1
   img = visualizeDecoding(buildDeepDecoder(decoders, #decoders), code)
   image.save("decoding" .. i .. ".png", img)
end

--FineTuning
--A present que l'on a ajusté les poids de chaque layer on fit
-- l'ensemble du réseau.
print("Fine tuning...en cours")
fit(deepClassifier, cec, trainData, trainLabels, 1e-1, 10)

-- Evaluation en train
pred = deepClassifier:forward(trainData)
__, pred = torch.max(pred,2)
print("score sur data train:")
print(torch.add(trainLabels:long(),-pred):eq(0):double():mean())

-- Evaluation en test
pred = deepClassifier:forward(testsData)
__, pred = torch.max(pred,2)
print("score sur data test:")
print(torch.add(testsLabels:long(),-pred):eq(0):double():mean())
