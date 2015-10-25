function [bow,nc]=computeBow(sifts,clusters,matNormClusters)
    % Coding 
    bow=zeros(size(clusters,1),1);
    nbClusters = size(clusters,1); % nb de clusters
    nbSifts = size(sifts,2); % nb de sifts
    
    nc=zeros(1, size(sifts,2));
    norme_xi_squar=sum(sifts.^2,1);
    norme_xi_squar_rep = repmat(norme_xi_squar,nbClusters,1);
    matNormClusters_rep = repmat(matNormClusters,1,nbSifts);
    
    res = norme_xi_squar_rep + matNormClusters_rep - 2*clusters*double(sifts);
    
    for i=1:size(res,2)
        colonne=res(:,i);
        [minimum, argmin]=min(colonne);
        nc(1,i)=argmin;
    end
    % Pooling 
    for c=1:nbClusters
       bow(c)=length(nc(nc==c));
       % Normalisation l1
       bow(c)=bow(c)/nbSifts;
    end   
end