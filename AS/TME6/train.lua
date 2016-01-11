--[[
prépraitement
se mettre dans le repertoire util puis
/projects/m2/as/tme6/as/util|
⇒  th prepare_data.lua -datadir ../data/perso
si le fichier input.txt est dans le dossier ../data/perso/
]]--


require 'torch'
require 'nn'
require 'nngraph'
require 'optim'
require 'lfs'

local CharLMMinibatchLoader=require 'util.CharLMMinibatchLoader'
local model_utils = require 'util.model_utils'
local LSTM = require 'model.LSTM'
local GRU = require 'model.GRU'
local RNN = require 'model.RNN'

cmd = torch.CmdLine()
cmd:text()
cmd:text('Train a character-level language model')
cmd:text()
cmd:text('Options')
-- data
cmd:option('-data_dir','data/tinyshakespeare','data directory. Should contain the file input.txt with input data')
-- model params
cmd:option('-latent_size', 10, 'size of LSTM internal state')
--md:option('-num_layers', 2, 'number of layers in the LSTM')
cmd:option('-model', 'lstm', 'lstm,gru or rnn')
-- optimization
cmd:option('-learning_rate',2e-3,'learning rate')
cmd:option('-seq_length',5,'number of timesteps to unroll for')
cmd:option('-batch_size',1,'number of sequences to train on in parallel')
cmd:option('-max_epochs',20,'number of full passes through the training data')
cmd:option('-temperature',1,'temperature of sampling')
cmd:option('-length_gen',30,'length of sequence to generate')
cmd:option('-seed_text','a','length of sequence to generate')
cmd:text()

-- parse input params
opt = cmd:parse(arg)
print(opt)


--load preparedData
loader=CharLMMinibatchLoader.create(opt.data_dir.."/data.t7",opt.data_dir.."/vocab.t7",opt.batch_size, opt.seq_length)
local vocab_size = loader.vocab_size  -- the number of distinct characters
local vocab = loader.vocab_mapping
print('vocab size: ' .. vocab_size)
size_voc = 0; for k,_ in pairs(loader.vocab_mapping) do size_voc=size_voc+1 end;
print ('size voc '..size_voc)
size_batches=0; for k,_ in pairs(loader.x_batches) do size_batches=size_batches+1 end;
print('nb batches '..size_batches)
--print(loader.x_batches[1])
--print (loader.vocab_mapping)
----convert to float and onehotencoder
--data = {}
--data['x'] = {}
--data['y'] = {}
--print ('nb batches'..#loader.x_batches)
--for i = 1, #loader.x_batches do
--    data['x'][i] = loader.x_batches[i]:int()
--    data['y'][i] = loader.y_batches[i]:int()
--end
--
----get size of dictionary
--function from_seq_to_vecs (seq, map, size_d)
--    vecs = {}
--    for i = 1, seq:size(2) do
--        t = torch.Tensor(size_d):zero()
--        t[seq[1][i]] = 1
--        vecs[i] = t
--    end
--    return vecs
--end
--n=0; for k,_ in pairs(loader.vocab_mapping) do n=n+1 end;
--size_d = n
--for i = 1, #loader.x_batches do
--    data['x'][i] = from_seq_to_vecs(data['x'][i], loader.vocab_mapping, size_d)
--    data['y'][i] = from_seq_to_vecs(data['y'][i], loader.vocab_mapping, size_d)
--end
--
--
--print (data['x'][1])
--print (data['x'][2])
--print(data['y'][1])

--h = latent
--x = donnees
function create_g(dim_x, dim_h)
    local input_h = nn.Identity()()
    local lx = nn.Linear(dim_h, dim_x)(input_h)
    local model_graph = nn.SoftMax()(lx)
    return nn.gModule({input_h}, {model_graph})
end

function create_h(dim_x, dim_h)
    local input_x = nn.Identity()()
    local input_h = nn.Identity()()

    local lx = nn.Linear(dim_x, dim_h)(input_x)
    local lh = nn.Linear(dim_h, dim_h)(input_h)

    local res = nn.CAddTable()({lx, lh})
    local model_graph = nn.Tanh()(res)

    return nn.gModule({input_h, input_x}, {model_graph})
end


--clonage

-- creation des modules h et g
dim_x= size_voc --espace d' entrée
dim_h= opt.latent_size --espace latent


local g = create_g(dim_x, dim_h)
local h = create_h(dim_x, dim_h)

nngraph.dot(g.fg, 'g_unittt', 'gen/g_unit')
--graph.dot(h.fg, 'h_unit', 'gen/h_unit')


local clones_g = model_utils.clone_many_times(g, opt.seq_length) --out layer
local clones_h = model_utils.clone_many_times(h, opt.seq_length) --input layer

function build_rnn(clones_g, clones_h)
    inputs = {nn.Identity()()}
    outputs = {}
    for t = 1, opt.seq_length do
        inputs[t+1] = nn.Identity()()
        if (t==1) then
            hnode = clones_h[t]({inputs[1], inputs[t+1]})
        else
            hnode = clones_h[t]({hnode, inputs[t+1]})
        end
        outputs[t] = clones_g[t](hnode)
    end

    return nn.gModule(inputs, outputs)
end
model = build_rnn(clones_g, clones_h)
--graph.dot(model.fg, 'rnn', 'gen/rnn')


losses = nn.ParallelCriterion()
for i = 1, opt.seq_length do
    losses:add(nn.ClassNLLCriterion(), 1/opt.seq_length)
end

--train model
function train_rnn(model, losses)
    for i = 1, opt.max_epochs do
        print('epoch:', i)
        local shuffle = torch.randperm(loader.nbatches)
        local mloss = 0
        for j = 1, loader.nbatches do
            local id = shuffle[j]
            local inputs_h = {}

            model:zeroGradParameters()
            inputs_h[1] = torch.Tensor(opt.latent_size):zero()
            for k = 1, opt.seq_length do
                inputs_h[k+1] = loader:vectorize(loader.x_batches[id][{1,k}])
            end
            local labels = {}
            --print(loader.y_batches[id]:squeeze())
            for k = 1, opt.seq_length do
                --labels[k] = loader:vectorize(loader.y_batches[id][{1,k}])
                --t = torch.Tensor(1)
                --t[1] = loader.y_batches[id][{1,k}]
                --labels[k] = t--loader.y_batches[id][{1,k}]
                labels[k] = loader.y_batches[id][{1,k}]
            end

            model:zeroGradParameters()
            local outputs = model:forward(inputs_h) -- logprobas see Softmax
            --print('inputs:', loader:decode_batch(loader.x_batches[id]))
            --print('labels:', loader:decode_batch(loader.y_batches[id]))
            --print('outputs:', loader:decode_outputs(outputs))
            --print('loss:', mloss)
            mloss = mloss + losses:forward(outputs, labels)
            local df_do = losses:backward(outputs, labels)
            local df_di = model:backward(inputs_h, df_do)
            model:updateParameters(opt.learning_rate)
        end
        print('iter:', i, 'loss:', mloss/loader.nbatches)
    end
end
--[[ to comment/decomment for training
train_rnn(model, losses)
--]]




--sample model

--get 1 unit from model
unit_h = clones_h[1]
unit_g = clones_g[1]
-- heat the unit
local seed_text = opt.seed_text
local gen_text = ''
print(opt.latent_size)
local input_h = torch.Tensor(opt.latent_size):zero()
if string.len(seed_text) > 0 then
    print('seeding with ' .. seed_text)
    print('--------------------------')
    for c in seed_text:gmatch'.' do --build an iterator on string for everything that matches the pattern
        prev_char = loader:vectorize(loader.vocab_mapping[c]) --c must belong to vocabulary
        gen_text = gen_text..c
        input_h = unit_h:forward{input_h, prev_char}
    end
end

-- start sampling/argmaxing
for i=1, opt.length_gen do
    -- log probabilities from the previous timestep
    --if opt.sample == 0 then
    --    -- use argmax
    --    local _, prev_char_ = prediction:max(2)
    --    prev_char = prev_char_:resize(1)

    -- forward the rnn for next character
    local lp = unit_g:forward(input_h) --mettre en accolade ?
    lp = lp:div(2)
    local probs = torch.exp(lp):squeeze()
    probs:div(torch.sum(probs)) -- renormalize so probs sum to one
    prev_char = torch.multinomial(probs:float(), 1):resize(1):float()[1]
    gen_text = gen_text..loader.number_mapping[prev_char]

    input_h = unit_h:forward{input_h, loader:vectorize(prev_char)}
end


opt.rundir = cmd:string('gen', opt, {dir=true})
paths.mkdir(opt.rundir)

-- create log file
cmd:log(opt.rundir .. '/log', opt)
print ('Generated Text : '..gen_text)
cmd:text(gen_text)
