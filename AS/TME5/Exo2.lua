--construction du reseau
require "nn"
require "nngraph"
local mnist = require 'mnist'
iteration = 100
input_size = 28*28

x = nn.Identity()()
un = nn.Identity()()
rho = nn.Linear(1,input_size)(un)
relu = nn.ReLU()(rho)
adamar = nn.CMulTable()({relu,x})
model_out = nn.Linear(input_size,input_size)(adamar)
model = nn.gModule({x,un},{model_out})

function optim(model,input,output,criterion,eps)

	
	for i=1, iteration do
			print("iteration "..i.." Loss = "..iteration_optim(input,output,model,criterion,eps))
	end
end

nbElts = 500
eps = 100.
trainset = mnist.traindataset()
X = (trainset.data[{{1,nbElts},{},{}}]:double() * (1./256.)):reshape(nbElts,28*28)
graph.dot(model.fg, 'Big MLP','test2')

optim(model2,X,X,nn.MSECriterion(),eps)
