function [ centers, error ] = solutionKMeans(points,M)

    squarenormExamples=0;
    centers=randomSeed(points, M);
    nc=assignementKMeans(points, centers, squarenormExamples);
    [centers, error, movecenters]= updateKMeans(points, centers, nc);
    errorTmp = 10;
    epsilon=0.01;

    while (abs(error - errorTmp)>epsilon)
        nc= assignementKMeans(points, centers, squarenormExamples);
        errorTmp = error;
        [centers, error, movecenters]=updateKMeans(points, centers, nc);
    end

end
