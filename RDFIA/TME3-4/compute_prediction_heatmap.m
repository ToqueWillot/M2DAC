function [ hmap ] = compute_prediction_heatmap( I, nc, w )
    %creation des regions
    
    [ r ] = denseSampling( I, 16, 8 );
    
    %on choisit le w assigné au meilleur mot et on normalise par le nombre
    %de regions
    best_w = w(nc)/size(r,2);
    
    %création de la hashmap
    hmap = zeros(size(I));
    
    %boucle sur chaque regions
    for i=1:size(r,2)
        hmap(r(1,i):(r(1,i)+15),r(2,i):(r(2,i)+15)) = best_w(i) + hmap(r(1,i):(r(1,i)+15),r(2,i):(r(2,i)+15));
    end
end