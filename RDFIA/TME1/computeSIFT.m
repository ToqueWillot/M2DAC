function [ sift ] = computeSIFT( s , Ig, Ior, Mg )

  sift = zeros(128,1);
  sift = sift + 2
end

a = [1,2,3,4;1,2,3,4;1,2,3,4;1,2,3,4]

s=16
for i=1:s
    ori(i,1) =  cos(2*pi*(i-1)/taille);
    ori(i,2) =  sin(2*pi*(i-1)/taille);
end