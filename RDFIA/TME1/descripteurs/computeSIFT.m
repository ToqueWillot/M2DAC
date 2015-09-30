function [sift] = computeSIFT(s, Ig, Ior, Mg, seuil)
% E_ Ig : module du gradient, matrice de taille s x s
% E_ Ior : orientation, matrice taille s x s [1:8]
% E_ s : 16
% E_ Mg : masque gaussien de taille s x s
% S_ sift : vecteur de taille 128  

if (nargin < 5)
    seuil = 0.5;
end

%s=16
patchSize = s/4;
%Ig = reshape(1:256, 16 ,16);
%Ior = ceil(rand(16)*8);
%Mg = ones(16,16);

H = zeros(8,4,4);

for i = 1:4
    for j = 1:4
        x = 1+(i-1)*4;
        y = 1+(j-1)*4;
        blocGr = Ig(x:x+patchSize-1, y:y+patchSize-1);
        blocOr = Ior(x:x+patchSize-1, y:y+patchSize-1);
        blocMg = Mg(x:x+patchSize-1, y:y+patchSize-1);
        for c = 1:8
            tmp = ((blocOr == c) .* blocGr .* blocMg);
            H(c,j,i) = sum(tmp(:));
        end
    end
end
sift = H(:);
if (norm(sift) < seuil)
    sift = zeros(128,1);
else
    sift = sift / norm(sift);
    sift(sift > 0.2) = 0.2;
    sift = sift / norm(sift);
end
end