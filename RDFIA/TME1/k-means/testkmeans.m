

eps=1;
d=2;
N=10;
K=2;
listPts = rand(N,d);
centers=rand(K,d);
squarenormExamples=repmat(sum(listPts.^2,2),1,K);
nc=assignementKMeans( listPts , centers , squarenormExamples);

for i:size(centers)
    u = (listPts(nc==i,:)
    if(size(u,1)==0)
        removeadd(i)
    end
    centersadd sum(u,1)/size(u,1)
    
end