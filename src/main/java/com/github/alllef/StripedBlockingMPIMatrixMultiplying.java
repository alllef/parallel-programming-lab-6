package com.github.alllef;

import mpi.MPI;

/*public class StripedBlockingMPIMatrixMultiplying implements MatrixMultiplying {
    @Override
    public int[][] multiply(int[][] firstMatr, int[][] secondMatr) {
        int MASTER = 0;
        int FROM_MASTER = 1;
        int FROM_WORKER = 2;
        int[][] resultMatr = new int[firstMatr.length][secondMatr[0].length];
        int numtasks = MPI.COMM_WORLD.Size();
        int taskId = MPI.COMM_WORLD.Rank();
        int numworkers = numtasks - 1;
        int[] rows = new int[1];
        int[] offset = new int[1];
        if (taskId == MASTER) {
            int averows = numworkers / firstMatr.length;
            int extrarows = numworkers % firstMatr.length;
            for (int dest = 1; dest <= numworkers; dest++) {
                rows[0] = (dest <= extrarows) ? averows + 1 : averows;
                MPI.COMM_WORLD.Send(firstMatr, offset[0], rows[0], MPI.OBJECT, dest, FROM_MASTER);
                MPI.COMM_WORLD.Send(secondMatr, 0, secondMatr.length, MPI.OBJECT, dest, FROM_MASTER);
                MPI.COMM_WORLD.Send(offset, 0, 1, MPI.INT, dest, FROM_MASTER);
                MPI.COMM_WORLD.Send(rows, 0, 1, MPI.INT, dest, FROM_MASTER);
                offset[0] = offset[0] + rows[0];
            }
            for (int source = 1; source <= numworkers; source++) {
                Object[] tmpMatr = new Object[resultMatr.length];

                MPI.COMM_WORLD.Recv(offset, 0, 1, MPI.INT, source, FROM_WORKER);
                MPI.COMM_WORLD.Recv(rows, 0, 1, MPI.INT, source, FROM_WORKER);
                MPI.COMM_WORLD.Recv(tmpMatr, offset[0], rows[0], MPI.OBJECT,
                        source, FROM_WORKER);
                for (int i = offset[0]; i < rows[0]; i++) {
                    resultMatr[i] = (int[]) tmpMatr[i];
                }
            }
        } else { /* if (taskid > MASTER)
            Object[] tmpFirstMatr = new Object[firstMatr.length];
            Object[] tmpSecondMatr = new Object[secondMatr.length];

            MPI.COMM_WORLD.Recv(offset, 0, 1, MPI.INT, MASTER, FROM_MASTER);
            MPI.COMM_WORLD.Recv(rows, 0, 1, MPI.INT, MASTER, FROM_MASTER);
            MPI.COMM_WORLD.Recv(tmpFirstMatr, offset[0], rows[0], MPI.OBJECT, MASTER, FROM_MASTER);
            MPI.COMM_WORLD.Recv(tmpSecondMatr, 0, secondMatr.length, MPI.OBJECT, MASTER, FROM_MASTER);
            for (int i = offset[0]; i < rows[0]; i++) {
                firstMatr[i] = (int[]) tmpFirstMatr[i];
            }

            for (int i = 0; i < secondMatr.length; i++) {
                secondMatr[i] = (int[]) tmpSecondMatr[i];
            }
            for (int k = 0; k < NCB; k++)
                for (int i = 0; i < rowsObj[0]; i++) {
                    c[i][k] = 0.0;
                    for (int j = 0; j < NCA; j++)
                        c[i][k] = c[i][k] + a[i][j] * b[j][k];
                }
            MPI.COMM_WORLD.Send(offset, 0, 1, MPI.INT, MASTER, FROM_WORKER);
            MPI.COMM_WORLD.Send(rows, 0, 1, MPI.INT, MASTER, FROM_WORKER);
            MPI.COMM_WORLD.Send(resultMatr, 0, rowsObj[0] * NCB, MPI.OBJECT, MASTER,
                    FROM_WORKER);
        }
        MPI.Finalize();
        return resultMatr;
    }

}*/
