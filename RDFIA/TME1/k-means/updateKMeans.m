function [ newcenters , erreur , movecenters ] = updateKMeans( listPts , centers  , nc)
	newcenters=centers;%??
    movecenters=0;
    erreur=0;
    for c=1:size(centers,1)
        moyenne = mean(listPts(nc==c,:));
        if(moyenne>0)
            for d=1:size(centers,2)
                newcenters(c,d) = moyenne(1,d);
            end
        else
           newcenters(c,:)=zeros(1,size(centers,2));
        end
size(listPts(nc==c,:))
size(newcenters(c,:))
        erreur=erreur+sum(sum(power(listPts(nc==c,:)-repelem(newcenters(c,:),size(listPts(nc==c,:),1),1),2)),2);
     end
    erreur
    movecenters=sum(sum(power(centers-newcenters,2),1),2);
end