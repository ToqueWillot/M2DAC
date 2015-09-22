----------------------------------------------------------------------
-- example-linear-regression.lua
-- 
-- This script provides a very simple step-by-step example of
-- linear regression, using Torch7's neural network (nn) package,
-- and the optimization package (optim).
--

-- note: to run this script, simply do:
-- torch script.lua

-- to run the script, and get an interactive shell once it terminates:
-- torch -i script.lua

-- we first require the necessary packages.
-- note: optim is a 3rd-party package, and needs to be installed
-- separately. This can be easily done using Torch7's package manager:
-- torch-pkg install optim

require 'torch'
require 'optim'
require 'nn'


----------------------------------------------------------------------
-- 1. Create the training data

-- In all regression problems, some training data needs to be 
-- provided. In a realistic scenarios, data comes from some database
-- or file system, and needs to be loaded from disk. In that 
-- tutorial, we create the data source as a Lua table.

-- In general, the data can be stored in arbitrary forms, and using
-- Lua's flexible table data structure is usually a good idea. 
-- Here we store the data as a Torch Tensor (2D Array), where each
-- row represents a training sample, and each column a variable. The
-- first column is the target variable, and the others are the
-- input variables.

-- The data are from an example in Schaum's Outline:
-- Dominick Salvator and Derrick Reagle
-- Shaum's Outline of Theory and Problems of Statistics and Economics
-- 2nd edition
-- McGraw-Hill
-- 2002

-- The data relate the amount of corn produced, given certain amounts
-- of fertilizer and insecticide. See p 157 of the text.

-- In this example, we want to be able to predict the amount of
-- corn produced, given the amount of fertilizer and intesticide used.
-- In other words: fertilizer & insecticide are our two input variables,
-- and corn is our target value.

--  {corn, fertilizer, insecticide}
data = torch.Tensor{
   {40,  6,  4},
   {44, 10,  4},
   {46, 12,  5},
   {48, 14,  7},
   {52, 16,  9},
   {58, 18, 12},
   {60, 22, 14},
   {68, 24, 20},
   {74, 26, 21},
   {80, 32, 24}
}


----------------------------------------------------------------------
-- 2. Define the model (predictor)

-- The model will have one layer (called a module), which takes the 
-- 2 inputs (fertilizer and insecticide) and produces the 1 output 
-- (corn).

-- Note that the Linear model specified below has 3 parameters:
--   1 for the weight assigned to fertilizer
--   1 for the weight assigned to insecticide
--   1 for the weight assigned to the bias term

-- In some other model specification schemes, one needs to augment the
-- training data to include a constant value of 1, but this isn't done
-- with the linear model.

-- The linear model must be held in a container. A sequential container
-- is appropriate since the outputs of each module become the inputs of 
-- the subsequent module in the model. In this case, there is only one
-- module. In more complex cases, multiple modules can be stacked using
-- the sequential container.

-- The modules are all defined in the neural network package, which is
-- named 'nn'.

ninputs = 2; noutputs = 1
model = nn.Linear(ninputs, noutputs) -- define the only module


----------------------------------------------------------------------
-- 3. Define a loss function, to be minimized.

-- In that example, we minimize the Mean Square Error (MSE) between
-- the predictions of our linear model and the groundtruth available
-- in the dataset.

-- Torch provides many common criterions to train neural networks.

criterion = nn.MSECriterion()


----------------------------------------------------------------------
-- 4. Train the model

-----------------  TODO 


----------------------------------------------------------------------
-- 5. Test the trained model.

-- Now that the model is trained, one can test it by evaluating it
-- on new samples.

-- The text solves the model exactly using matrix techniques and determines
-- that 
--   corn = 31.98 + 0.65 * fertilizer + 1.11 * insecticides

-- We compare our approximate results with the text's results.

text = {40.32, 42.92, 45.33, 48.85, 52.37, 57, 61.82, 69.78, 72.19, 79.42}

print('id  approx   text')
for i = 1,(#data)[1] do
   local myPrediction = model:forward(data[i][{{2,3}}])
   print(string.format("%2d  %6.2f %6.2f", i, myPrediction[1], text[i]))
end

