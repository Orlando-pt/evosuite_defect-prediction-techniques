package org.evosuite.coverage.aes;

import java.util.ArrayList;
import java.util.List;
import java.io.*;

import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

//mycode starts
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.io.ClassPathResource;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.api.ndarray.INDArray;
//mycode ends

public abstract class AbstractAESCoverageSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = 5184507726269266351L;
	private static int iteration = 0; //mycode

	public static enum Metric { AES, DTR, VDDU,VMDDU,VRDDU,VCDDU,VCMDDU1,VCMDDU2};		//New enum added 1
	private Metric metric;

	public AbstractAESCoverageSuiteFitness(Metric metric) {
		this.metric = metric;
	}

	public double getMetric(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {
		List<ExecutionResult> results = runTestSuite(suite);
		Spectrum spectrum = getSpectrum(results);
		return getMetric(spectrum);
	}

	public double getBasicCoverage(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {
		List<ExecutionResult> results = runTestSuite(suite);
		Spectrum spectrum = getSpectrum(results);
		return spectrum.basicCoverage();
	}

	@Override
	public double getFitness(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {
		double metric_value = getMetric(suite);
		double fitness = metricToFitness(metric_value);

		updateIndividual(this, suite, fitness);
		suite.setCoverage(this, metric_value);

		return fitness;
	}

	protected abstract Spectrum getSpectrum(List<ExecutionResult> results);

    public static void appendStrToFile(String fileName,
                                       String str)
    {
        try {

            // Open given file in append mode.
            BufferedWriter out = new BufferedWriter(
                    new FileWriter(fileName, true));
            out.write(str);
            out.close();
        }
        catch (IOException e) {
            System.out.println("exception occoured" + e);
        }
    }

    public ArrayList<String> getMean(String file_path)
    {
        ArrayList<String> means = new ArrayList<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(
                    file_path));
            String line = reader.readLine();
            while (line != null) {
                means.add(line);
                // read next line
                line = reader.readLine();

            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return means;

    }

    public ArrayList<String> getStd_dev(String file_path)
    {
        ArrayList<String> std_devs = new ArrayList<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(
                    file_path));
            String line = reader.readLine();
            while (line != null) {
                std_devs.add(line);
                // read next line
                line = reader.readLine();

            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return std_devs;

    }



    public double[] powerset(double[] features,int size)
    {
        int pow_set_size = ((int)Math.pow(2, size));
        double powset[] = new double[pow_set_size-1];
        int pow_index = 0;
        for(int counter=1;counter<pow_set_size;counter++)
        {
            double temp = 1.0;
            for(int j=0;j<size;j++)
            {
                if((counter & (1 << j)) > 0)
                {
                    temp = temp * features[j];
                }

            }
            powset[pow_index] = temp;
            pow_index++;
        }

        return powset;

    }

    public double getMetric(Spectrum spectrum) {
		switch (this.metric) {
        case DTR:
            return spectrum.getDistinctTransactionsRho() * spectrum.getAmbiguity();

        case VDDU:
            return spectrum.getVrho() * (1.0 - spectrum.getSimpson()) * spectrum.getAmbiguity();
        //Changed VMDDU flag to return VDDU * branch_coverage
        //case VMDDU:
        //return spectrum.getVMrho() * (1.0 - spectrum.getSimpson()) * spectrum.getAmbiguity();
        case VMDDU:{
            //mycode starts
            //lambda implementation

            iteration++;
            Aj aj = spectrum.getVrho2();
            double rho_component = aj.getvcd();
            double rho_transaction = aj.getvrd();


            //double result =  ((0.0294493 * spectrum.basicCoverage()) + (( -0.82273926) * spectrum.getRho()) + (0.10903152 * (1 - spectrum.getSimpson())) + ((-0.39320752) * spectrum.getAmbiguity()) +
            //        ((-0.33318195) * rho_transaction) + ((-1.00889138) * rho_component) +  (0.73490905));


            double[] mydata = new double[6];
            mydata[0] = spectrum.basicCoverage();
            mydata[1] = spectrum.getRho();
            mydata[2] = (1 - spectrum.getSimpson());
            mydata[3] = spectrum.getAmbiguity();
            mydata[4] = rho_transaction;
            mydata[5] = rho_component;
            int matrix_size = spectrum.getNumTransactions();


            String data_dump = "";
            data_dump = String.valueOf(iteration) + "," + String.valueOf(mydata[0]) + "," + String.valueOf(mydata[1]) + "," + String.valueOf(mydata[2]) + ","
                    + String.valueOf(mydata[3]) + "," + String.valueOf(mydata[4]) + "," + String.valueOf(mydata[5]) + ","
                    + String.valueOf(matrix_size);

            //normalize the features
            mydata[0] = (mydata[0] - 6.528579662598990030e-01) / 3.091870017158382389e-01;
            mydata[1] = (mydata[1] - 2.830355552505142147e-01) / 2.559463383939822312e-01;
            mydata[2] = (mydata[2] - 8.741161545340463412e-01) / 2.935632591125947877e-01;
            mydata[3] = (mydata[3] - 2.417415102720089359e-01) / 1.476727560690869467e-01;
            mydata[4] = (mydata[4] - 9.075997634198873509e-01) / 7.665414057345444621e-02;
            mydata[5] = (mydata[5] - 6.171747989139511059e-01) / 2.047115140756818608e-01;



            INDArray test_data = Nd4j.create(1, 6);
            INDArray myrow = Nd4j.create(mydata);
            test_data.putRow(0, myrow);
            String model_path = "";
            MultiLayerNetwork model;
            BufferedReader br = null;
            File file = new File("/tmp/model_path6.txt");


            try {
                br = new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                System.out.println("File doesn't exist");
                e.printStackTrace();
                //return;
            }
            try {
                model_path = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {

                model = KerasModelImport.importKerasSequentialModelAndWeights(model_path);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            double result = model.output(test_data).getDouble(0);


            if (result < 0)
                result = 0;
            else if (result > 1)
                result = 1;


            double lambda = 1 - Math.min(1, (((double) iteration) / 10000));

            double new_result = (lambda * spectrum.basicCoverage()) + ((1 - lambda) * (1 - result));


            data_dump = "Start" + "," + data_dump + "," + String.valueOf(result) + "," + String.valueOf(new_result) + "," +"end" + "\n";
            appendStrToFile("/tmp/feature_dump_Time_VMDDU.csv", data_dump);

            return (0.5 * new_result);
            //mycode ends
        }
        //return spectrum.getVrho() * (1.0 - spectrum.getSimpson()) * spectrum.getAmbiguity() * spectrum.basicCoverage();
        case VCDDU:{


            //mycode starts
            //switching implementation

            iteration++;

            Aj aj = spectrum.getVrho2();
            double rho_component = aj.getvcd();
            double rho_transaction = aj.getvrd();


            //double result =  ((0.0294493 * spectrum.basicCoverage()) + (( -0.82273926) * spectrum.getRho()) + (0.10903152 * (1 - spectrum.getSimpson())) + ((-0.39320752) * spectrum.getAmbiguity()) +
            //        ((-0.33318195) * rho_transaction) + ((-1.00889138) * rho_component) +  (0.73490905));


            double[] mydata = new double[6];
            mydata[0] = spectrum.basicCoverage();
            mydata[1] = spectrum.getRho();
            mydata[2] = (1 - spectrum.getSimpson());
            mydata[3] = spectrum.getAmbiguity();
            mydata[4] = rho_transaction;
            mydata[5] = rho_component;
            int matrix_size = spectrum.getNumTransactions();


            String data_dump = "";
            data_dump = String.valueOf(iteration) + "," + String.valueOf(mydata[0]) + "," + String.valueOf(mydata[1]) + "," + String.valueOf(mydata[2]) + ","
                    + String.valueOf(mydata[3]) + "," + String.valueOf(mydata[4]) + "," + String.valueOf(mydata[5]) + ","
                    + String.valueOf(matrix_size);


            mydata[0] = (mydata[0] - 6.528579662598990030e-01) / 3.091870017158382389e-01;
            mydata[1] = (mydata[1] - 2.830355552505142147e-01) / 2.559463383939822312e-01;
            mydata[2] = (mydata[2] - 8.741161545340463412e-01) / 2.935632591125947877e-01;
            mydata[3] = (mydata[3] - 2.417415102720089359e-01) / 1.476727560690869467e-01;
            mydata[4] = (mydata[4] - 9.075997634198873509e-01) / 7.665414057345444621e-02;
            mydata[5] = (mydata[5] - 6.171747989139511059e-01) / 2.047115140756818608e-01;


            //if iteration number is less than c then return branch coverage metric, else return nn prediction
            if (iteration <= 1000 && (spectrum.basicCoverage() <= 0.6)) {
                data_dump = "Start" + "," + data_dump + "," + String.valueOf(spectrum.basicCoverage()) + "," + "0" + "end" + "\n";
                appendStrToFile("/tmp/feature_dump_VCDDU.csv", data_dump);
                //data_dump = "";
                return (0.5 * spectrum.basicCoverage());
            }

            INDArray test_data = Nd4j.create(1, 6);
            INDArray myrow = Nd4j.create(mydata);
            test_data.putRow(0, myrow);
            String model_path = "";
            MultiLayerNetwork model;
            BufferedReader br = null;
            File file = new File("/tmp/model_path5.txt");
            //File file = new File("/home/abhijit/Thesis/repos/evo_vddu/evo_iteration_dump/iteration_number.txt");

            try {
                br = new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                System.out.println("File doesn't exist");
                e.printStackTrace();
                //return;
            }
            try {
                model_path = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                //String simpleMlp = new ClassPathResource("/home/abhijit/Desktop/test_model.h5").getFile().getPath();
                // String simpleMlp = "/home/abhijit/Desktop/test_model.h5";
                // System.out.println(simpleMlp);
                model = KerasModelImport.importKerasSequentialModelAndWeights(model_path);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            double result = model.output(test_data).getDouble(0);


            if (result < 0)
                result = 0;
            else if (result > 1)
                result = 1;
            //return (0.5*spectrum.basicCoverage())*(0.5 - (0.5 * result));


            data_dump = "Start" + "," + data_dump + "," + String.valueOf(result) + "," + "1" + "end" + "\n";
            appendStrToFile("/tmp/feature_dump_VCDDU.csv", data_dump);
            //data_dump = "";
            return (0.5 - (0.5 * result));
        }
            //mycode ends
			// return spectrum.getVCrho() * (1.0 - spectrum.getSimpson()) * spectrum.getAmbiguity();
		case VCMDDU1:{
            //mycode starts
            iteration++;
            //distance feature added
            Aj aj = spectrum.getVrho2();
            double rho_component = aj.getvcd();
            double rho_transaction = aj.getvrd();
            double[] distances = spectrum.getDistances();

            double[] mydata = new double[9];
            mydata[0] = spectrum.basicCoverage();
            mydata[1] = spectrum.getRho();
            mydata[2] = (1 - spectrum.getSimpson());
            mydata[3] = spectrum.getAmbiguity();
            mydata[4] = rho_transaction;
            mydata[5] = rho_component;
            mydata[6] = distances[0];
            mydata[7] = distances[1];
            mydata[8] = distances[2];
            //int matrix_size = spectrum.getNumTransactions();

            //normalise the data
            ArrayList<String> means = getMean("/tmp/mean_VCMDDU1");
            ArrayList<String> std_devs = getStd_dev("/tmp/std_dev_VCMDDU1");

            for(int i=0;i<means.size();i++)
            {
                double std_dev  = Double.parseDouble(std_devs.get(i));
                if(std_dev == 0)
                    mydata[i] = 0.0;
                else
                    mydata[i] = (mydata[i] - (Double.parseDouble(means.get(i)))) / std_dev;
            }



            INDArray test_data = Nd4j.create(1, 9);
            INDArray myrow = Nd4j.create(mydata);
            test_data.putRow(0, myrow);
            String model_path = "";
            MultiLayerNetwork model;
            BufferedReader br = null;
            File file = new File("/tmp/model_path7.txt");
            //File file = new File("/home/abhijit/Thesis/repos/evo_vddu/evo_iteration_dump/iteration_number.txt");

            try {
                br = new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                System.out.println("File doesn't exist");
                e.printStackTrace();
                //return;
            }
            try {
                model_path = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {

                model = KerasModelImport.importKerasSequentialModelAndWeights(model_path);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            double result = model.output(test_data).getDouble(0);



            if (result < 0)
                result = 0;
            else if (result > 1)
                result = 1;

            double lambda = 1 - Math.min(1, (((double) iteration) / 10000));

            double new_result = (lambda * spectrum.basicCoverage()) + ((1 - lambda) * (1 - result));


            return (0.5 * new_result);

        }
        //mycode ends
			//return spectrum.getVCMrho1() * (1.0 - spectrum.getSimpson()) * spectrum.getAmbiguity();
		case VCMDDU2:{
            //mycode starts

            //feature power set implementation
            Aj aj = spectrum.getVrho2();
            double rho_component = aj.getvcd();
            double rho_transaction = aj.getvrd();
            double[] distances = spectrum.getDistances();


            double[] mydata = new double[9];
            mydata[0] = spectrum.basicCoverage();
            mydata[1] = spectrum.getRho();
            mydata[2] = (1 - spectrum.getSimpson());
            mydata[3] = spectrum.getAmbiguity();
            mydata[4] = rho_transaction;
            mydata[5] = rho_component;
            mydata[6] = distances[0];
            mydata[7] = distances[1];
            mydata[8] = distances[2];
            //int matrix_size = spectrum.getNumTransactions();

            //first normalise the 9 features
            ArrayList<String> means1 = getMean("/tmp/mean1_VCMDDU2");
            ArrayList<String> std_devs1 = getStd_dev("/tmp/std_dev1_VCMDDU2");

            for(int i=0;i<means1.size();i++)
            {
                double std_dev  = Double.parseDouble(std_devs1.get(i));
                if(std_dev == 0)
                    mydata[i] = 0.0;
                else
                    mydata[i] = (mydata[i] - (Double.parseDouble(means1.get(i)))) / std_dev;
            }

            //compute the power set
            double pow_feature_set[] = powerset(mydata,9);

            //normalise the power set
            ArrayList<String> means2 = getMean("/tmp/mean2_VCMDDU2");
            ArrayList<String> std_devs2 = getStd_dev("/tmp/std_dev2_VCMDDU2");

            for(int i=0;i<means2.size();i++)
            {
                double std_dev  = Double.parseDouble(std_devs2.get(i));
                if(std_dev == 0)
                    pow_feature_set[i] = 0.0;
                else
                    pow_feature_set[i] = (pow_feature_set[i] - (Double.parseDouble(means2.get(i)))) / std_dev;
            }


            INDArray test_data = Nd4j.create(1, 511);
            INDArray myrow = Nd4j.create(pow_feature_set);
            test_data.putRow(0, myrow);
            String model_path = "";
            MultiLayerNetwork model;
            BufferedReader br = null;
            File file = new File("/tmp/model_path8.txt");
            //File file = new File("/home/abhijit/Thesis/repos/evo_vddu/evo_iteration_dump/iteration_number.txt");

            try {
                br = new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                System.out.println("File doesn't exist");
                e.printStackTrace();
                //return;
            }
            try {
                model_path = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {

                model = KerasModelImport.importKerasSequentialModelAndWeights(model_path);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            double result = model.output(test_data).getDouble(0);



            if (result < 0)
                result = 0;
            else if (result > 1)
                result = 1;


            return (0.5 - (0.5 * result));
        }
        //mycode ends
			//return spectrum.getVCMrho2() * (1.0 - spectrum.getSimpson()) * spectrum.getAmbiguity();
		case VRDDU:
		    return spectrum.getVRrho();
		case AES:
		default:
			 return spectrum.getRho() * (1.0 - spectrum.getSimpson()) * spectrum.getAmbiguity();                   
		}
	}

	public static double metricToFitness(double metric) {
		return Math.abs(0.5d - metric);
	}

}
