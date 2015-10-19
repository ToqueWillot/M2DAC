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



--------TME4
require 'nn'
mnist = require 'mnist'


--Data mnist
trainset = mnist.traindataset()
testset = mnist.testdataset()

print(trainset.size) -- to retrieve the size
print(testset.size) -- to retrieve the size

x_train = trainset.data -- the input (a 28x28 ByteTensor)
y_train = trainset.label -- the label (0--9)

x_test = testset.data -- the input (a 28x28 ByteTensor)
y_test = testset.label -- the label (0--9)


--Function to resize a matrix
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


--todo
-- function keepCategories(list,x,y) do
--   for i=1,list:size(1) do
--     local selected = indices[y:eq(list[i])]
--     new_x:add(x(selected))
--     new_y:add(y(selected))
--   end
--   return new_x,new_y
-- end

--todo

-- TRAIN function gradient descent
function train(mlp, criterion, data, labels, lr, nIter)
   local lr = lr or 1e-5
   local nIter = nIter or 100
   for i = 1,nIter do
      mlp:zeroGradParameters()
      y = mlp:forward(data)
      loss = criterion:forward(y,labels)
      df_do = criterion:backward(y,labels)
      df_di = mlp:backward(data, y)
      mlp:updateParameters(lr)
   end
end

-- Layers creation
layerSize = {28*28,
	     14*14,
	     50,
	     25,
	     28*28}

layers = {}
for i=1,(#layerSize)-1 do
   table.insert(layers, nn.Linear(layerSize[i],layerSize[i+1]))
end


-- Auto_encoders creation
autoEncoders = {}
for i=1,#layers do
   autoEncoder = nn.Sequential()
   autoEncoder:add(layers[i])
   autoEncoder:add(nn.Tanh())
   autoEncoder:add(nn.Linear(layerSize[i+1], layerSize[i]))
   table.insert(autoEncoders, autoEncoder)
end


-- Auto_encoders training
mse = nn.MSECriterion()
stack = nn.Sequential()
for i=1,(#autoEncoders)-1 do
   print(i)
   x = stack:forward(trainData)
   train(autoEncoders[i], mse, x, x)
   stack:add(layers[i])
   stack:add(nn.Tanh())
end
i = (#autoEncoders)
x = stack:forward(trainData)
train(autoEncoders[i], mse, x, x)


-- Add the final classifier
classifier = nn.Linear(layerSize[i], nClass)
nll = nn.ClassNLLCriterion()
x = stack:forward(trainData)
train(classifieur, nll, x, trainLabels)
stack:add(classifier)


-- Finetuning ( train the model already train until the classifier )
train(stack, nll, trainData, trainLabels, 1e-5, 100)






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
