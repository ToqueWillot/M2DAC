function [ sifts ] = computeSIFTsImage( I, seuil )

if (nargin < 2)
    seuil = 0.5;
end

s = 16;
delta = 8;
r = denseSampling(I, s, delta);
%drawRectsImage(I, r, s);

hx = [-1 0 1];
hy = [1;2;1];

Ix = convolution_separable(I, hx, hy);
Iy = convolution_separable(I, transpose(hy), transpose(hx));

Ig = sqrt((Ix .^ 2) + (Iy .^ 2));
%Ig = (Ix .^ 2) + (Iy .^ 2);
Ior = orientation(Ix, Iy, Ig);
Mg = gaussSIFT(s);

sifts = zeros(128,size(r,2));
for i = 1:size(r,2)
    x = r(1,i);
    y = r(2,i);
    patchIg = Ig(x:x+16-1, y:y+16-1);
    patchIor = Ior(x:x+16-1, y:y+16-1);
    sifts(:,i) = computeSIFT(s, patchIg, patchIor, Mg, seuil);
end
end