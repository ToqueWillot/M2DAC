

addpath('/Users/floriantoque/Documents/UPMC/M2DAC_git/RDFIA/TME1/k-means/');
pathDes='/Users/floriantoque/Documents/UPMC/M2DAC_git/RDFIA/TME1/Sifts/';
pathSav='/Users/floriantoque/Documents/UPMC/M2DAC_git/RDFIA/TME1/KMeans/';

[points,normes] = randomSampling(pathDes);

M=1000;
[centers, error] = solutionKMeans(points, M);
minError= error;
T=50;

for t=1:T
    [centers,error]=solutionKMeans(points, M);
    if error<minError
        minError=error;
        bestCenters=centers;
    end
    disp(t)
    disp('---------------------------------------------')
    disp(t)
end

save(strcat(pathSav,'dictionnaireVisuel.m'),'bestCenters')