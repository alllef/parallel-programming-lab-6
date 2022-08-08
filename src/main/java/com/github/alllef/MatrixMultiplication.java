package com.github.alllef;

import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import mpi.*;

import java.io.FileWriter;
import java.io.IOException;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.exit;

public class MatrixMultiplication {
    public static void main(String[] args) {
        int workersNum = 8;
        int matrixSize = 1024;
        int NRA = matrixSize;
        int NCA = matrixSize;
        int NCB = matrixSize;
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
                i, j, k, rc;
        int[] rows = new int[1];
        int[] offset = new int[1];

        double[][] a = new double[NRA][NCA]; /* matrix A to be multiplied */
        double[][] b = new double[NCA][NCB]; /* matrix B to be multiplied */
        double[][] c = new double[NRA][NCB];//matrix C with results
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

            double start = System.currentTimeMillis();
            for (dest = 1; dest <= numworkers; dest++) {
                rows[0] = (dest <= extra) ? averow + 1 : averow;
                System.out.printf("Sending %d rows to task %d offset= %d\n%n",
                        rows[0], dest, offset[0]);
                MPI.COMM_WORLD.Send(rows, 0, 1, MPI.INT, dest, FROM_MASTER);
                MPI.COMM_WORLD.Send(offset, 0, 1, MPI.INT, dest, FROM_MASTER);
                MPI.COMM_WORLD.Send(a, offset[0], rows[0], MPI.OBJECT, dest,
                        FROM_MASTER);
                MPI.COMM_WORLD.Send(b, 0, NCA, MPI.OBJECT, dest, FROM_MASTER);
                offset[0] = offset[0] + rows[0];
            }
            /* Receive results from worker tasks */
            for (source = 1; source <= numworkers; source++) {
                Object[] tmpMatr = new Object[c.length];
                MPI.COMM_WORLD.Recv(offset, 0, 1, MPI.INT, source, FROM_WORKER);
                MPI.COMM_WORLD.Recv(rows, 0, 1, MPI.INT, source, FROM_WORKER);
                MPI.COMM_WORLD.Recv(tmpMatr, offset[0], rows[0], MPI.OBJECT,
                        source, FROM_WORKER);
                for (int index = offset[0]; index < offset[0] + rows[0]; index++) {
                    c[index] = (double[]) tmpMatr[index];
                }
                System.out.printf("Received results from task %d offset %d rows %d\n", source, offset[0], rows[0]);
            }
            double finish = MPI.Wtime();
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

            try (CSVWriter csvWriter = new CSVWriter(new FileWriter("results.csv", true))) {
                StatefulBeanToCsv<ResultsBean> converter = new StatefulBeanToCsvBuilder<ResultsBean>(csvWriter)
                        .build();
                System.out.println(start);
                System.out.println(finish);
                double timeInMillis = System.currentTimeMillis();
                System.out.println(timeInMillis);
                ResultsBean resultsBean = new ResultsBean(workersNum, matrixSize, (timeInMillis-start));
                converter.write(resultsBean);
            } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
                e.printStackTrace();
            }
        }
/******** worker task *****************/
        else { /* if (taskid > MASTER) */
            Object[] tmpFirstMatr = new Object[NRA];
            Object[] tmpSecondMatr = new Object[NCA];
            MPI.COMM_WORLD.Recv(rows, 0, 1, MPI.INT, MASTER, FROM_MASTER);
            MPI.COMM_WORLD.Recv(offset, 0, 1, MPI.INT, MASTER, FROM_MASTER);
            MPI.COMM_WORLD.Recv(tmpFirstMatr, offset[0], rows[0], MPI.OBJECT, MASTER, FROM_MASTER);
            MPI.COMM_WORLD.Recv(tmpSecondMatr, 0, NCA, MPI.OBJECT, MASTER, FROM_MASTER);

            for (int index = offset[0]; index < offset[0] + rows[0]; index++) {
                a[index] = (double[]) tmpFirstMatr[index];
            }

            for (int index = 0; index < NCA; index++) {
                b[index] = (double[]) tmpSecondMatr[index];
            }

            for (k = 0; k < NCB; k++) {
                for (i = offset[0]; i < offset[0] + rows[0]; i++) {
                    c[i][k] = 0.0;
                    for (j = 0; j < NCA; j++)
                        c[i][k] = c[i][k] + a[i][j] * b[j][k];
                }
            }
            MPI.COMM_WORLD.Send(offset, 0, 1, MPI.INT, MASTER, FROM_WORKER);
            MPI.COMM_WORLD.Send(rows, 0, 1, MPI.INT, MASTER, FROM_WORKER);
            MPI.COMM_WORLD.Send(c, offset[0], rows[0], MPI.OBJECT, MASTER,
                    FROM_WORKER);
        }
        MPI.Finalize();
    }
}
