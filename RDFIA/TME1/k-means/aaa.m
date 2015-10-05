function [nc]=aaa(listPts, centers, squarenormExamples) %Renvoie la lsite des plus proches clusters
   N = size(listPts,1);
   M = size(centers,1);

   nc=zeros(1, size(listPts,1));
   
   norme_xi_squar=sum(listPts.^2,2)';
   norme_cn_squar=sum(centers.^2,2);
   
   norme_xi_squar_rep = repmat(norme_xi_squar,M,1);
   norme_cn_squar_rep = repmat(norme_cn_squar,1,N);

   res = norme_xi_squar_rep + norme_cn_squar_rep - 2*centers*listPts';
   
   for i=1:size(res,2)
       colonne=res(:,i);
       [minimum, argmin]=min(colonne);
       nc(1,i)=argmin;
   end
end
