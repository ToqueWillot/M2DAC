require 'torch'
require 'optim'
require 'nn'
require 'gnuplot'
require 'math'


x = torch.Tensor(200,2)
y = torch.Tensor(200,1)

for i=1,100 do
    x[i]=torch.randn(2)
    x[i][1]=x[i][1]+3
    x[i][2]=x[i][2]+6
    y[i]=torch.Tensor(1):fill(1)
end

for i=101,200 do
    x[i]=torch.randn(2)
    x[i][1]=x[i][1]+1
    x[i][2]=x[i][2]+2
    y[i]=torch.Tensor(1):fill(-1)
end

gnuplot.plot(x)


-- starting
ITERATIONS=100
GRADIENT_STEP=0.1
N=2


module = nn.Linear(2,1)  -- <theta,x>
module:reset(0.01)
criterion = nn.MSECriterion() -- (^y - y)^2

epsilon = 1e-3

-- or
-- criterion = nn.MarginCriterion()
for i=1,ITERATIONS do
  local idx = math.random(N)                      -- x au hasard
  --local idx = math.floor(torch.uniform(1,(#x)+1))
  module:zeroGradParameters()                     -- grad_theta <- zero
  local y_prime = module:forward(x[idx])          -- f_theta(x)
  local loss = criterion:forward(y_prime,y[idx])  -- delta(f_theta(x),y)
  local delta = criterion:backward(y_prime,y[idx])--
  module:backward(x[idx],delta)                   -- grad_theta <- grad_theta + grad_theta * delta (f_theta,y)
  module:updateParameters(epsilon)          -- theta <- theta - Epsilon * grad_theta
  -- prints occasionels
end


-- batch
local MIN = -10
local MAX = 10
local S = 100
local step = (20)/S
mat = torch.Tensor(S,S)

for x = 1,100 do
  for y = 1,100 do
    local input = torch.Tensor(2)         -- coordonees de la case
    input[1] = (x - 1)*step + MIN         --
    input[2] = (y - 1)*step + MIN
    local output = module:forward(input)
    mat[x][y] = output[1]
  end
end

print(mat)

gnuplot.plot(mat)


--[[
 Faire le kenerl trick
]]
