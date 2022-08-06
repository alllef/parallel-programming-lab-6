package com.github.alllef;

import mpi.*;

import static java.lang.System.exit;

public class MatrixMultiplication {
    public static void main(String[] args) {
        int NRA = 62;
        int NCA = 15;
        int NCB = 7;
        int MASTER = 0;
        int FROM_MASTER = 1;
        int FROM_WORKER = 2;
        int numtasks,
                taskid,
                numworkers,
                source,
                dest,
                /* rows of matrix A sent to each worker */
                averow, extra,
                i, j, k, rc, offset, rows;
        int[] rowsObj = new int[1];
        int[] offsObj = new int[1];

        double a[][] = new double[NRA][NCA]; /* matrix A to be multiplied */
        double b[][] = new double[NCA][NCB]; /* matrix B to be multiplied */
        double c[][] = new double[NRA][NCB];//matrix C with results
        MPI.Init(args);

        numtasks = MPI.COMM_WORLD.Size();
        taskid = MPI.COMM_WORLD.Rank();
        if (numtasks < 2) {
            System.out.println("Need at least two MPI tasks. Quitting...\n");
            MPI.COMM_WORLD.Abort(1);
            exit(1);
        }
        numworkers = numtasks - 1;
        /**************************** master task ************************************/
        if (taskid == MASTER) {
            System.out.println("mpi_mm has started with tasks.\n" + numtasks);
            for (i = 0; i < NRA; i++)
                for (j = 0; j < NCA; j++)
                    a[i][j] = 10d;
            for (i = 0; i < NCA; i++)
                for (j = 0; j < NCB; j++)
                    b[i][j] = 10d;
            /* Send matrix data to the worker tasks */
            averow = NRA / numworkers;
            extra = NRA % numworkers;
            offset = 0;
            for (dest = 1; dest <= numworkers; dest++) {
                rows = (dest <= extra) ? averow + 1 : averow;
                System.out.printf("Sending %d rows to task %d offset= %d\n%n",
                        rows, dest, offset);
                rowsObj[0] =  rows;
                offsObj[0] = offset;
                MPI.COMM_WORLD.Send(offsObj, 0, 1, MPI.INT, dest, FROM_MASTER);
                MPI.COMM_WORLD.Send(rowsObj, 0, 1, MPI.OBJECT, dest, FROM_MASTER);
                MPI.COMM_WORLD.Send(a[offset][0], 0, rows * NCA, MPI.DOUBLE, dest,
                        FROM_MASTER);
                MPI.COMM_WORLD.Send(b, 0, NCA * NCB, MPI.DOUBLE, dest, FROM_MASTER);
                offset = offset + rows;
            }
            /* Receive results from worker tasks */
            for (source = 1; source <= numworkers; source++) {
                MPI.COMM_WORLD.Recv(offsObj, 0, 1, MPI.OBJECT, source, FROM_WORKER);
                MPI.COMM_WORLD.Recv(rowsObj, 0, 1, MPI.OBJECT, source, FROM_WORKER);
                MPI.COMM_WORLD.Recv(c[offset][0], 0,  rowsObj[0] * NCB, MPI.DOUBLE,
                        source, FROM_WORKER);
                System.out.printf("Received results from task %d\n"/*,id*/);
            }
            /* Print results */
            System.out.println("****\n");
            System.out.println("Result Matrix:\n");
            for (i = 0; i < NRA; i++) {
                System.out.println("\n");
                for (j = 0; j < NCB; j++)
                    System.out.printf("%6.2f ", c[i][j]);
            }
            System.out.println("\n********\n");
            System.out.println("Done.\n");
        }
/******** worker task *****************/
        else { /* if (taskid > MASTER) */
            MPI.COMM_WORLD.Recv(offsObj, 0, 1, MPI.OBJECT, MASTER, FROM_MASTER);
            MPI.COMM_WORLD.Recv(rowsObj, 0, 1, MPI.OBJECT, MASTER, FROM_MASTER);
            MPI.COMM_WORLD.Recv(a, 0, rowsObj[0] * NCA, MPI.DOUBLE, MASTER, FROM_MASTER);
            MPI.COMM_WORLD.Recv(b, 0, NCA * NCB, MPI.DOUBLE, MASTER, FROM_MASTER);
            for (k = 0; k < NCB; k++)
                for (i = 0; i < rowsObj[0]; i++) {
                    c[i][k] = 0.0;
                    for (j = 0; j < NCA; j++)
                        c[i][k] = c[i][k] + a[i][j] * b[j][k];
                }
            MPI.COMM_WORLD.Send(offsObj, 0, 1, MPI.OBJECT, MASTER, FROM_WORKER);
            MPI.COMM_WORLD.Send(rowsObj, 0, 1, MPI.OBJECT, MASTER, FROM_WORKER);
            MPI.COMM_WORLD.Send(c, 0, rowsObj[0] * NCB, MPI.DOUBLE, MASTER,
                    FROM_WORKER);
        }
        MPI.Finalize();
    }
}
