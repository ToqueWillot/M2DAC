function [nc]=assignementKMeans2(listPts, centres, squarenormExamples) 
   N = size(listPts,1);
   M = size(centres,1);

   nc=zeros(1, size(listPts,1));
   
   norme_xi_squar=sum(listPts.^2,2)';
   norme_cn_squar=sum(centres.^2,2);
   
   norme_xi_squar_rep = repmat(norme_xi_squar,M,1);
   norme_cn_squar_rep = repmat(norme_cn_squar,1,N);

   res = norme_xi_squar_rep + norme_cn_squar_rep - 2*centres*listPts';
   
   for i=1:size(res,2)
       colonne=res(:,i);
       [minimum, argmin]=min(colonne);
       nc(1,i)=argmin;
   end
end