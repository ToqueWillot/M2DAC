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


--__________test gaussian generation_________________
-- local n=3; local position={3,3}
-- local x_test = create_x(n,position);
-- print(x_test)
--
-- local n=3; local position={3,3}; label=4
-- local x,y = create_x_y(n,position,label);
-- print(x)
-- print(y)
--
-- local params = {{3,{3,3},4},{3,{-3,-3},2}}
-- local x,y = create_multiclass_xy (params)
-- print(x)
-- print(y)



--Plot the world___________________________________

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

--test plot______________________
local params = {{10,{3,3},1},{10,{-3,-3},-1}}
local xxx,yyy = create_multiclass_xy (params)
labels={1,-1}
colors={"red","blue"}
name="test points.png"

-- print(xxx)
-- print(yyy)
-- plot_points(xxx,yyy,labels,colors,name)

-- gridX=getRegionGrid(xxx,80)
-- y = torch.ones(gridX:size(1))
-- print("taillegridX"..gridX:size(1))
-- plot_points(gridX,y,{1},{"red"},"gridred.png")




--fit the model with x and y
function fit_model(x,y,model,nIter,ep,criterion,layer)
  for i = 1,nIter do
     model:zeroGradParameters()
     y_chap = model:forward(x)
     loss = criterion:forward(y_chap, y)
     grad = criterion:backward(y_chap, y)
     model:backward(x, grad)
     model:updateParameters(ep)
  end
  return model
end





-- ______classif_linear_2gauss
-- exemple of classification with linear model. criterion = MSE (mean squared error)
local params = {{20,{3,3},1},{20,{-3,-3},-1}}
local x,y = create_multiclass_xy (params) -- create data x (gaussian) and y (labels)
labels={1,-1}
colors={"red","blue"}
colorsGrid={"pink","cyan"}
name="classif_linear_2gauss.png"

nIter=5000
ep=0.0005
criterion=nn.MSECriterion()
layer={2,1} --2 inputs (x[1], x[2]) one output (label)

model = nn.Linear(layer[1],layer[2])
modelfit = fit_model(x,y,model,nIter,ep,criterion,layer)

plot_decision(x,y,modelfit,labels,colors,colorsGrid,name)




--______xor model lineaire
-- le model lineaire ne permet pas de séparer l'espace en trois
local params = {{20,{3,3},1},
                {20,{-3,-3},1},
                {20,{-3,3},-1},
                {20,{3,-3},-1}}
local x,y = create_multiclass_xy (params)
labels={1,-1}
colors={"red","blue"}
colorsGrid={"pink","cyan"}
name="classif_linear_XOR.png"

nIter=10000
ep=0.0001
criterion=nn.MSECriterion()
layer={2,1}
model = nn.Linear(layer[1],layer[2])
modelfit = fit_model(x,y,model,nIter,ep,criterion,layer)

plot_decision(x,y,modelfit,labels,colors,colorsGrid,name)







--_____xor kernel tricks___________________________
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
colors={"red","blue"}
colorsGrid={"pink","cyan"}
name="classif_linear_XOR_KernelTrick.png"

nIter=50000
ep=0.00005
criterion=nn.MSECriterion()
layer={3,1} --3 inputs (x[1], x[2], x[1]*x[2]) one output (label)
model = nn.Linear(layer[1],layer[2])
modelfit = fit_model(new_x,y,model,nIter,ep,criterion,layer2)
plot_decision3dim(x,y,modelfit,labels,colors,colorsGrid,name)



--_____xor reseau de neuronnes 3 couches___________________________
local params = {{20,{3,3},1},
                {20,{-3,-3},1},
                {20,{-3,3},-1},
                {20,{3,-3},-1}}
local x,y = create_multiclass_xy (params)


--[[A laide d'un reseau de neuronnes à trois couches il est possible de classifier
les données "XOR" . la couche cachée représente la fonction tanh (permettant de
découpé l'espace en elipse)]]--

labels={1,-1}
colors={"red","blue"}
colorsGrid={"pink","cyan"}
name="classif_linear_XOR_NN3Layer.png"

nIter=50000
ep=0.0005
criterion=nn.MSECriterion()
model=nn.Sequential()
model:add(nn.Linear(2,3))
model:add(nn.Tanh(true))
model:add(nn.Linear(3,1))
modelfit = fit_model(x,y,model,nIter,ep,criterion,layer2)
plot_decision(x,y,modelfit,labels,colors,colorsGrid,name)
