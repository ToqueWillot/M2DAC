clear all;
close all;


cate=categories();

pathSav='/Users/floriantoque/Documents/UPMC/M2DAC_git/RDFIA/TME3-4';
patchmin='/Users/floriantoque/Documents/UPMC/M2DAC_git/RDFIA/TME3-4/patchmin';
nomDico='/Users/floriantoque/Documents/UPMC/M2DAC_git/RDFIA/TME1/KMEANS/dictionnaireVisuel';
pathSifts='/Users/floriantoque/Documents/UPMC/M2DAC_git/RDFIA/TME1/Sifts/';
pathim='/Users/floriantoque/Documents/UPMC/M2DAC_git/RDFIA/TME1/Scene/';
pathdes='/Users/floriantoque/Documents/UPMC/M2DAC_git/RDFIA/TME3-4/Bows/';


s=16;
delta=8;

catinit=1;
catend=15;

load(nomDico);

load(patchmin);


for index=catinit:catend
    cat=cate{index};
    % CREATION DU DOSSIER DESCRIPTEURS POUR LA CLASSE SI INEXISTANT
    pathcat = strcat(pathdes,cat,'/');
    mkdir(pathcat);
    if(exist(pathcat)==0)
        mkdir(pathcat);
    end
    
    tstarttot= tic;

    direc = strcat(pathim,cat,'/');
    listima=dir([direc '*.jpg'] );
    dirSifts = strcat(pathSifts,cat,'/');
    listSifts=dir([dirSifts '*.mat'] );
    n=length(listima);
    
    
    % CALCUL DES DESCRIPTEURS POUR LA CLASSE INDEX 
    for num=1:n
        % Ouverture image
        [I]=imread(strcat(direc,listima(num).name));

        %Calcul du sift de l'image
        load(strcat(dirSifts,listSifts(num).name));

        % Calcul de matormClusters
        matNormClusters=sum(bestCentres.^2,2);
        
        % Calcul du Bow
        [bow,nc]=computeBow(sifts, bestCentres, matNormClusters); % Comment obtient-on best centres ?

        desname = strcat(pathcat,listima(num).name(1:length(listima(num).name)-4),'.mat');

        save(desname,'bow');
        
        if(mod(num,100)==0)
            strcat('nb patchs=',num2str(size(sifts,2)))
            tcal = toc(tstarttot);
            strcat('temps de calcul =',num2str(tcal))
        end
    end
    
      
    
    tcaltot = toc(tstarttot);
    
    
    strcat('computation time for category :',cat,' =',num2str(tcaltot))
    
    
end

