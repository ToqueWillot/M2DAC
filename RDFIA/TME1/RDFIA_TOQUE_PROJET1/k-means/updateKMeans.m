function [ newcenters , erreur , movecenters ] = updateKMeans( listPts , centers  , nc)
	newcenters=centers;
    movecenters=0;
    erreur=0;
    
    for c=1:size(centers,1)
        if(size(listPts(nc==c,:),1)>0)
            newcenters(c,:) = mean(listPts(nc==c,:));
        else
           newcenters(c,:)=zeros(1,size(centers,2));
        end
        erreur=erreur+sum(sum(power(listPts(nc==c,:)-repelem(newcenters(c,:),size(listPts(nc==c,:),1),1),2)),2);
    end
    movecenters=sum(sum(power(centers-newcenters,2),1),2);
end