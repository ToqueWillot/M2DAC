seuil = 0.75;
s = 16;
delta = 8; 

dir = '/Users/floriantoque/Documents/UPMC/M2DAC_git/RDFIA/Scene/';

I = randomImage(dir);
%I = imread(strcat(dir,'MITcoast/image_0088.jpg'));


r = denseSampling(I, s, delta);

sifts = computeSIFTsImage(I, seuil);
drawPatches(I,r,s,sifts);