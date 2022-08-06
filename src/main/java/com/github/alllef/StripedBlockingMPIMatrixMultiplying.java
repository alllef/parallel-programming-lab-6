package com.github.alllef;

import mpi.MPI;

public class StripedBlockingMPIMatrixMultiplying implements MatrixMultiplying {
    @Override
    public int[][] multiply(int[][] firstMatr, int[][] secondMatr) {
        int MASTER = 0;
        int FROM_MASTER = 1;
        int FROM_WORKER = 2;
        int[][] resultMatr = new int[firstMatr.length][secondMatr[0].length];
        int numtasks = MPI.COMM_WORLD.Size();
        int taskId = MPI.COMM_WORLD.Rank();
        int numworkers = numtasks - 1;
        int averows = numworkers / firstMatr.length;
        int extrarows = numworkers % firstMatr.length;
        int[] rows = new int[1];
        int[] offset = new int[1];
        for (int dest = 1; dest <= numworkers; dest++) {
            rows[0] = (dest <= extrarows) ? averows + 1 : averows;
            MPI.COMM_WORLD.Send(firstMatr, offset[0], rows[0], MPI.OBJECT, dest, FROM_MASTER);
            MPI.COMM_WORLD.Send(secondMatr, 0, secondMatr.length, MPI.OBJECT, dest, FROM_MASTER);

        }
    }
}
