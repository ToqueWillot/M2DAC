function [ bow,nc ] = computeBow( sifts, clusters , matNormClusters)
squarenormExamples=0
bow=zeros(size(clusters,1))

[ nc ] = assignementKMeans( sifts , clusters , squarenormExamples)

for i=1:size(clusters,1)
    bow[i]+=1
end

bow=bow/sum(bow)


    %% nailed it
end

