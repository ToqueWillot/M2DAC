function [ nc ] = assignementKMeans( listPts , centers , squarenormExamples)

  K= size(centers,1)
  N=size(listPts,1)
  A = repelem(listPts,K,1)
  B = repmat(centers,N,1)

  dist = sqrt(sum((A-B).^2,2))
  V = reshape(dist,[K,N])'

  [M,nc] = min(V,[],2)
  nc = reshape(nc,[1,N])

end