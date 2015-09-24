%a = [1,2,3,4;1,2,3,4;1,2,3,4;1,2,3,4];

I=imread('/Users/floriantoque/Documents/UPMC/M2DAC_git/RDFIA/image_0001.jpg','jpg');
s=16;
delta=8;


r=denseSampling(I,s,delta);
drawRectsImage(I,r,s);


a = [1,2,3,4;1,2,3,4;1,2,3,4;1,2,3,4]

newI=I(1:16,1:16)
matriceRes = []
s=16
for i=1:s/4
    newMatrice = []
    for j=1:s/4
        newMatrice = [newMatrice,newI(i:i+4,j:j+4)]
    end
    matriceRes = [matriceRes,newMatrice]
end

