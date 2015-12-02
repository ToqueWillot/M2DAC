require "nn"
require "nngraph"
local mnist = require 'mnist'
iteration = 100
function buildExo1(size)
	seq1 = nn.Sequential()

	seq1:add(nn.Linear(size,size))
	seq1:add(nn.Sigmoid())

	
	
	concatTable = nn.ConcatTable():add(seq1):add(nn.Identity())
	addTable = nn.CAddTable()
	seq2 = nn.Sequential()
	seq2:add(concatTable)
	seq2:add(addTable)
	return seq2
end

function buildExo1NNGraph(size)
	local x = nn.Identity()()
	local term1 = nn.Sigmoid()(nn.Linear(size,size)(x))

	local model_graph = nn.CAddTable()({term1,x})
	
	local model = nn.gModule({x},{model_graph}) 
	
	return model
end



--graph.dot(model_nng.fg, 'Big MLP','./test')
function iteration_optim(input,output,model,criterion,eps)
	model:zeroGradParameters()
	local pred = model:forward(input)
	local loss = criterion:forward(pred,output)
	model:backward(input, criterion:backward(pred,output))
	model:updateParameters(eps)
	return loss
end

function Exo1OptimSansModelComplet(input,output,criterion,eps)
	local add = nn.CAddTable()
	
	local linASig = nn.Sequential()
	linASig:add(nn.Linear(input:size(2),input:size(2)))
	linASig:add(nn.Sigmoid())

	
	for i=1, iteration do
		linASig:zeroGradParameters()
		local a = linASig:forward(x)
		local b = add:forward({a,x})
		
		local loss = criterion:forward(b,output)
		local dloss = criterion:backward(b,output)
	
		local db = add:backward({a,x},dloss)[1]
		local da = linASig:backward(x,db)
		linASig:updateParameters(eps)
		print("iteration i Loss = "..loss)
	end
end
function optim(model,input,output,criterion,eps)

	
	for i=1, iteration do
			print("iteration "..i.." Loss = "..iteration_optim(input,output,model,criterion,eps))
	end
end
nbElts = 500
eps = 100.
trainset = mnist.traindataset()
X = (trainset.data[{{1,nbElts},{},{}}]:double() * (1./256.)):reshape(nbElts,28*28)

model1 = buildExo1(28*28)
model2 = buildExo1NNGraph(28*28)
optim(model2,X,X,nn.MSECriterion(),eps)
