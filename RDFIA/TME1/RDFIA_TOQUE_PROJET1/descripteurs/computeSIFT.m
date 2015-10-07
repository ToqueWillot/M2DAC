function [sift] = computeSIFT(s, Ig, Ior, Mg, seuil)


if (nargin < 5)
    seuil = 0.5;
end


patchSize = s/4;

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