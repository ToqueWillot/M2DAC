require 'torch'
require 'optim'
require 'nn'
require 'gnuplot'
require 'math'


--__________Gaussian generation main function(create_multiclass_xy)_____________

function create_x(n,position)
  x=torch.randn(n,#position)
  for i=1,#position do
      x[{{},{i}}]=x[{{},{i}}]+position[i]
  end
  return x
end

function create_x_y(n,position,label)
  x=create_x(n,position)
  y=torch.ones(n)*label
  return x,y
end

-- params = tensor(tensor(n,position,label))
--have to concat with axis 1 for x!!!!
function create_multiclass_xy(params)
  local x,y = create_x_y(params[1][1],params[1][2],params[1][3])
  for i = 2,#params do
    xx,yy=create_x_y(params[i][1],params[i][2],params[i][3])
    x = x:cat(xx,1)
    y = y:cat(yy)
  end
  return x,y
end


-- --__________test gaussian generation_________________
-- -- local n=3; local position={3,3}
-- -- local x_test = create_x(n,position);
-- -- print(x_test)
-- --
-- -- local n=3; local position={3,3}; label=4
-- -- local x,y = create_x_y(n,position,label);
-- -- print(x)
-- -- print(y)
-- --
-- -- local params = {{3,{3,3},4},{3,{-3,-3},2}}
-- -- local x,y = create_multiclass_xy (params)
-- -- print(x)
-- -- print(y)
--
--
--
-- --Plot the world___________________________________

function toPlot2D(x,label,color)
   return {label, x[{{},1}], x[{{},2}], 'p ls 1 lc rgb "'..color..'"'}
end


function plot_points(x, y, labels, colors, name)
    toPlot = {}
    indices = torch.linspace(1,(#y)[1],(#y)[1]):long()
    for i = 1, #labels do
       local selected = indices[y:eq(labels[i])]
       table.insert(toPlot, toPlot2D(x:index(1, selected), "classe "..labels[i], colors[i]))
    end
    gnuplot.pngfigure(name)
    gnuplot.plot(toPlot)
    gnuplot.close()
end

function getPointsGrid(x,nbPoints)
   local xmin = x:min(1)[1]
   local xmax = x:max(1)[1]
   local axe1 = torch.linspace(xmin[1], xmax[1],nbPoints)
   local axe2 = torch.linspace(xmin[2], xmax[2],nbPoints)
   local gridX = torch.zeros(axe1:size(1) * axe2:size(1),2)
   i = 1
   for i1 = 1, axe1:size(1) do
      for i2 = 1, axe2:size(1) do
         gridX[i][1] = axe1[i1]
         gridX[i][2] = axe2[i2]
         i = i + 1
      end
   end
   return gridX
end


function getGridtoPlot(model,x,labels,colorsGrid)
  toPlot = {}
  xGrid = getPointsGrid(x,100)
  xGridInputs = xGrid
  yGrid = model:forward(xGrid)
  yGrid = yGrid:sign()
  indices = torch.linspace(1,yGrid:size(1),yGrid:size(1)):long()
  for i = 1, #labels do
     selected = indices[yGrid:eq(labels[i])]
     table.insert(toPlot, toPlot2D(xGrid:index(1, selected), "grid "..labels[i], colorsGrid[i]))
  end
  return toPlot
end

function getGridtoPlot3couches(couche1,couche2,couche3,x,labels,colorsGrid)
  toPlot = {}
  xGrid = getPointsGrid(x,100)
  xGridInputs = xGrid
  local yGrid11 = couche1:forward(xGrid)
  local yGrid22 = couche2:forward(yGrid11)
  local yGrid33 = couche3:forward(yGrid22)
  local yGrid = yGrid33:sign()

  indices = torch.linspace(1,yGrid:size(1),yGrid:size(1)):long()
  for i = 1, #labels do
     local selected = indices[yGrid:eq(labels[i])]
     table.insert(toPlot, toPlot2D(xGrid:index(1, selected), "grid "..labels[i], colorsGrid[i]))
  end
  return toPlot
end


function getPointstoPlot(x,y,labels,colors)
  toPlot = {}
  indices = torch.linspace(1,(#y)[1],(#y)[1]):long()
  for i = 1, #labels do
     selected = indices[y:eq(labels[i])]
     table.insert(toPlot, toPlot2D(x:index(1, selected), "classe "..labels[i], colors[i]))
  end
  return toPlot
end

function plot_decision(x,y,model,labels,colors,colorsGrid,name)
  local toPlot = getGridtoPlot(model,x,labels,colorsGrid)
  local toPlot2 = getPointstoPlot(x,y,labels,colors)
  for i=1,#toPlot2 do
    table.insert(toPlot, toPlot2[i])
  end
  gnuplot.pngfigure(name)
  gnuplot.plot(toPlot)
  gnuplot.close()
end

function plot_decision3couches(x,y,couche1,couche2,couche3,labels,colors,colorsGrid,name)
  local toPlot = getGridtoPlot3couches(couche1,couche2,couche3,x,labels,colorsGrid)
  local toPlot2 = getPointstoPlot(x,y,labels,colors)
  for i=1,#toPlot2 do
    table.insert(toPlot, toPlot2[i])
  end
  gnuplot.pngfigure(name)
  gnuplot.plot(toPlot)
  gnuplot.close()
end

function getGridtoPlot3dim(model,x,labels,colorsGrid)
  toPlot = {}
  xGrid = getPointsGrid(x,100)
  xGridInputs = torch.cat(xGrid, torch.cmul(xGrid[{{},1}], xGrid[{{},2}]),2)
  yGrid = model:forward(xGridInputs)
  yGrid = yGrid:sign()
  indices = torch.linspace(1,yGrid:size(1),yGrid:size(1)):long()
  for i = 1, #labels do
     local selected = indices[yGrid:eq(labels[i])]
     table.insert(toPlot, toPlot2D(xGrid:index(1, selected), "grid "..labels[i], colorsGrid[i]))
  end
  return toPlot
end

function plot_decision3dim(x,y,model,labels,colors,colorsGrid,name)
  local toPlot = getGridtoPlot3dim(model,x,labels,colorsGrid)
  local toPlot2 = getPointstoPlot(x,y,labels,colors)
  for i=1,#toPlot2 do
    table.insert(toPlot, toPlot2[i])
  end
  gnuplot.pngfigure(name)
  gnuplot.plot(toPlot)
  --gnuplot.plot(toPlot2)
  gnuplot.close()
end

-- --test plot______________________
-- local params = {{10,{3,3},1},{10,{-3,-3},-1}}
-- local xxx,yyy = create_multiclass_xy (params)
-- labels={1,-1}
-- colors={"red","blue"}
-- name="test points.png"
--
-- -- print(xxx)
-- -- print(yyy)
-- -- plot_points(xxx,yyy,labels,colors,name)
--
-- -- gridX=getRegionGrid(xxx,80)
-- -- y = torch.ones(gridX:size(1))
-- -- print("taillegridX"..gridX:size(1))
-- -- plot_points(gridX,y,{1},{"red"},"gridred.png")
--


---------------------------------------------------------------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------------------------------------------------------------
-----------------------------------------------------Commencement du TME 3 ------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------------------------------------------------------------
--Binome Florian Toqué et Paul Willot
--Master 2 DAC , UE AS (apprentissage statistique)
--Professeur Patrick Gallinari et Ludovic Denoyer

--TME3
--Exercice 1 implémentation de la fonction d'activation Requ et d'un model linéaire !!! :)

--1.1 implémentation de la fonction d'activation Requ

-- la fonction requ n'a pas de parametre on ne met donc pas a jour ses poids. (sa derivée en fonction de ses parametres est nulle)

local ReQu, parent = torch.class('nn.ReQu', 'nn.Module')

function ReQu:__init()
   parent.__init(self)
end

function ReQu:updateOutput(input)
   self.output = torch.pow(input,2)
   self.inputIsPositive = input:ge(0):double() -- condition if de la fonction requ ( => probleme classif xor en non lineaire)
   self.output:cmul(self.inputIsPositive)
   return self.output
end

function ReQu:updateGradInput(input, gradOutput)
   -- On veut le gradient du Loss en fonction de notre entrée (input).
   -- grad input egal gradoutput * grad de la fonction d'erreur en fonction des x (inputs)
   self.gradInput = input * 2
   self.gradInput:cmul(self.inputIsPositive)
   -- Ci dessous, gradOutput est le gradient du loss en fonction des
   -- entrées du module suivant (ou sorties du module actuel)
   self.gradInput:cmul(gradOutput)
   return self.gradInput
end



--1.2 implémentation de la fonction Lineaire
-- la fonction requ n'a pas de parametre on ne met donc pas a jour ses poids. (sa derivée en fonction de ses parametres est nulle)

local Lineaire, Parent = torch.class('nn.Lineaire', 'nn.Module')

function Lineaire:__init(inputSize, outputSize)
   Parent.__init(self)
   self.weight = torch.Tensor(outputSize, inputSize)
   self.gradWeight = torch.Tensor(outputSize, inputSize)
   self:reset()
end

--fonction utilisée par le forward on calcul l'output
function Lineaire:updateOutput(x)
   if x:dim() == 1 then
       self.output = self.weight * x -- si entrée = vecteur
   else
       self.output = x * self.weight:t() -- si entrée = matrice
   end
   return self.output
end

function Lineaire:updateGradInput(x, gradOutput)
    -- Le gradient des sorties dune fonction lineaire en fonction des entrées est égal aux paramètres (theta*x)'/x'=theta d'où
   self.gradInput = self.weight:transpose(1,2) * gradOutput
   return self.gradInput
end

--mise à jour des parametres
-- multiplication de gradoutput par (x :qui est la derivée de la fonction lineaire en fonction des parametres)
function Lineaire:accGradParameters(x, gradOutput)
   self.gradWeight = self.gradWeight + gradOutput:reshape(gradOutput:size(1),1) * x:reshape(1,x:size(1))
   return self.gradWeight
end

    -- Initialisation des paramètres entre -1 et 1
function Lineaire:reset()
    self.weight = self.weight:uniform() * 2 - 1
end



-- 1.3 test sur des données jouets de la fonction lineaire

-- ______classif_linear_2gauss
-- exemple of classification with lineaire. criterion = MSE (mean squared error) donées jouets
local params = {{20,{3,3},1},{20,{-3,-3},-1}}
local x,y = create_multiclass_xy (params) -- create data x (gaussian) and y (labels)
labels={1,-1}
colors={"blue","yellow"}
colorsGrid={"grey","pink"}
name="classif_lineaire_2gauss.png"

nIter=1000
ep=0.00001
criterion=nn.MSECriterion()
layer={2,1} --2 inputs (x[1], x[2]) one output (label)

model = nn.Lineaire(layer[1],layer[2])
for i = 1, nIter do
   shuffle = torch.randperm(x:size(1))
   for j = 1, x:size(1) do
      id = shuffle[j]
      input = x[id]
      label = torch.Tensor{y[id]}
      model:zeroGradParameters()
      output = model:forward(input)
      loss = criterion:forward(output, label)
      df_doutput = criterion:backward(output, label)
      df_dinput = model:backward(input, df_doutput)
      model:updateParameters(ep)
   end
end
-- print(x)
-- print(y)
-- print(model:forward(x):sign())
plot_decision(x,y,model,labels,colors,colorsGrid,name)

--la fonction lineaire separe classifie sans soucis les données jouets.




--exercice 2 xor kernel tricks___________________________
local params = {{20,{3,3},1},
                {20,{-3,-3},1},
                {20,{-3,3},-1},
                {20,{3,-3},-1}}
local x,y = create_multiclass_xy (params)


--kernel_trick ajout de dimension aux données dentrées
--[[ le nombre de dimension de x ne permet pas au model lineaire
de séparer l'espace comme il faut pour classifier les données correctement
il faut rajouter une dimension x[1]*x[2] ainsi meme le modele lineaire peut classifier correctement les données.]]--
local new_x= torch.cat(x, torch.cmul(x[{{},1}],x[{{},2}]) ,2)

labels={1,-1}
colors={"blue","yellow"}
colorsGrid={"grey","pink"}
name="classif_lineaire_XOR_kerneltrick.png"

nIter=1000
ep=0.00005
criterion=nn.MSECriterion()
layer={3,1} --3 inputs (x[1], x[2], x[1]*x[2]) one output (label)
model = nn.Lineaire(layer[1],layer[2])

for i = 1, nIter do
   shuffle = torch.randperm(new_x:size(1))
   for j = 1, new_x:size(1) do
      id = shuffle[j]
      input = new_x[id]
      label = torch.Tensor{y[id]}
      model:zeroGradParameters()
      output = model:forward(input)
      loss = criterion:forward(output, label)
      df_doutput = criterion:backward(output, label)
      df_dinput = model:backward(input, df_doutput)
      model:updateParameters(ep)
   end
end
-- print(new_x)
-- print(y)
-- print(model:forward(new_x):sign())


plot_decision3dim(new_x,y,model,labels,colors,colorsGrid,name)
--A laide du kernel trick la fonction linéaire arrive sans soucis à classifié lensemble des points



-- 1.3 Résolution du XOR sans kerneltrick( reseau de neuronnes à trois couche lineaire, requ, lineaire)

-- ______data xor
local params = {{20,{3,3},1},
                {20,{-3,-3},1},
                {20,{-3,3},-1},
                {20,{3,-3},-1}}
local x,y = create_multiclass_xy (params)

labels={1,-1}
colors={"blue","yellow"}
colorsGrid={"grey","pink"}
name="classif_XOR_nonLineaire.png"

nIter=2000
ep=0.00001
criterion=nn.MSECriterion()
layer1={2,3} --2 inputs (x[1], x[2]) one output (label)
layer3={3,1}

local couche1 = nn.Lineaire(layer1[1],layer1[2])
local couche2 = nn.ReQu()
local couche3 = nn.Lineaire(layer3[1],layer3[2])
for i = 1, nIter do
   shuffle = torch.randperm(x:size(1))
   for j = 1, x:size(1) do
      id = shuffle[j]
      input = x[id]
      label = torch.Tensor{y[id]}
      couche1:zeroGradParameters()
      couche2:zeroGradParameters()
      --forward pour chaque couche
      output1 = couche1:forward(input)
      output2 = couche2:forward(output1)
      output3 = couche3:forward(output2)
      --loss et backward du criterion pour avoir derive du loss en fonction de son output
      loss = criterion:forward(output3, label)
      df_doutput3 = criterion:backward(output3, label)

      --propagation du gradient de l'erreur sur les couches précédentes
      df_dinput3 = couche3:backward(output2, df_doutput3)
      df_dinput2 = couche2:backward(output1, df_dinput3)
      df_dinput1 = couche1:backward(input, df_dinput2)

      couche1:updateParameters(ep)
      couche3:updateParameters(ep)
   end
end

--
-- print(x)
-- print(y)
plot_decision3couches(x,y,couche1,couche2,couche3,labels,colors,colorsGrid,name)

--on remarque ici que l'une des parties est mal classifié ceci est du au fait que la fonction requ mette à 0 les valeurs negatives.
-- Lors de la classification une partie des points obtient le label 0 qui n'existe pas. la back propagation ne permet pas
--de mettre a jour les poids etant donné que la derivée de la fonction d'erreur en fonction des poids est nulle.






--------TME4

local mnist = require 'mnist'

local trainset = mnist.traindataset()
local testset = mnist.testdataset()

print(trainset.size) -- to retrieve the size
print(testset.size) -- to retrieve the size

local ex = trainset[i]
local x = ex.x -- the input (a 28x28 ByteTensor)
local y = ex.y -- the label (0--9)


--1 image 28x28 => 14x14

sm=4
mat = torch.rand(sm, sm)

function resizeMat(mat)
  new_mat=torch.Tensor(mat:size(1)/2,mat:size(2)/2)
  for i=0,mat:size(1)-1,2 do
    for j=0,mat:size(2)-1,2 do
      new_mat[i/2+1][j/2+1] = mat[i+1][j+1]
    end
  end
  return new_mat * (1.0/256)
end


function keepCategories(list,x,y) do
  for i=1,list:size(1) do
    local selected = indices[y:eq(list[i])]
    new_x:add(x(selected))
    new_y:add(y(selected))
  end
  return new_x,new_y
end

function auto_encoder(x,size) do

  criterion=nn.MSECriterion()
  local couche1 = nn.Lineaire(x:size(1),size)
  local couche2 = nn.Tanh()
  local couche3 = nn.Lineaire(size,x:size(1))

  for i = 1, nIter do
     shuffle = torch.randperm(x:size(1))
     for j = 1, x:size(1) do
        id = shuffle[j]
        input = x[id]
        label = torch.Tensor{y[id]}
        couche1:zeroGradParameters()
        couche2:zeroGradParameters()
        --forward pour chaque couche
        output1 = couche1:forward(input)
        output2 = couche2:forward(output1)
        output3 = couche3:forward(output2)
        --loss et backward du criterion pour avoir derive du loss en fonction de son output
        loss = criterion:forward(output3, label)
        df_doutput3 = criterion:backward(output3, label)

        --propagation du gradient de l'erreur sur les couches précédentes
        df_dinput3 = couche3:backward(output2, df_doutput3)
        df_dinput2 = couche2:backward(output1, df_dinput3)
        df_dinput1 = couche1:backward(input, df_dinput2)

        couche1:updateParameters(ep)
        couche3:updateParameters(ep)
     end

  return couche1,couche2,couche3

end
