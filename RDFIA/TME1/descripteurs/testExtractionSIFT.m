s = 16;
delta = 8; 
dir = '/Users/floriantoque/Documents/UPMC/M2DAC_git/RDFIA/Scene/';
I = randomImage(dir);
sampling = denseSampling(I, s, delta);
r = ceil(rand(1)*size(sampling,2));
x = sampling(1,r);
y = sampling(1,r);
%-----------------------------------%
path =  '/Users/floriantoque/Documents/UPMC/M2DAC_git/RDFIA/Scene/kitchen/image_0138.jpg';
I = imread(path);
x = 17;
y = 217;
%-----------------------------------%
%path = '/users/nfs/Etu3/3000693/Documents/RDFIA/TME1/tools.gif';
%I = imread(path);
%------------%
%x = 80;
%y = 200;
%------------%
%x = 173;
%y = 250;
%-----------------------------------%
%------------%
%I = marche();
%x=1;
%y=1;
%x = 125;
%y = 100;
%------------%
%x = 97;
%y = 121;
%------------%
%x = 121;
%y = 121;
%-----------------------------------%


hx = [-1 0 1];
hy = [1;2;1];

Ix = convolution_separable(I, hx, hy);
Iy = convolution_separable(I, transpose(hy), transpose(hx));

Ig = sqrt((Ix .^ 2) + (Iy .^ 2));
%Ig = (Ix .^ 2) + (Iy .^ 2);
Ior = orientation(Ix, Iy, Ig);
Mg = gaussSIFT(s);

patchIg = Ig(x:x+16-1, y:y+16-1);
patchIor = Ior(x:x+16-1, y:y+16-1);
sift = computeSIFT(s, patchIg, patchIor, Mg);
visuSIFT(I, Ig, Ior, [x;y], 'marche', s, sift);