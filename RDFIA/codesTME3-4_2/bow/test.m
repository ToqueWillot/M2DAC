sifts=1;
clusters=2;
matNormClusters=2;
[ bow,nc ] = computeBow( sifts, clusters , matNormClusters);

sifts=[1,2,3;6,6,6;9,9,9];
clusters=[5,5,5,2;8,8,8,2;1,2,2,2];
squareNE=0;

[nc] = assignementKMeans(sifts,clusters,squareNE);

sifts.' .* 

size(clusters.')