package grader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashMap;

import student.MySpaceship;

public class a8Grader {
    /* These should all be round numbers. They are doubles to allow for multiple
     * seeds to be run, but the final grade will be integral. */
    private static final double RESCUE_CORRECTNESS= 45;
    private static final double RETURN_CORRECTNESS= 40;
    private static final double RESCUE_OPTIMALITY= 5;
    private static final double RETURN_OPTIMALITY= 10;

    private static boolean printed= false; // true iff print occurs
    private static final int PRINT_PENALTY= 4; // deducted iff print occurs

    private static final long[] RESCUE_OPTIMALITY_SEEDS= new long[]
            { -60111111L            // neighbors, but basic chooses poorly
                    , -333333L              // close together, centralized
                    , -9999L                // close together near edge
                    , -3357383822589138305L // close together, somewhat sparse edges
                    , 5378428136332632818L  // close together
                    , 2468959635154320336L  // close together near edge
                    , 8649022763586768876L  // close together
            };

    private static final long[] RETURN_OPTIMALITY_SEEDS= new long[]
            { -60111111L            // neighbors
                    , -3357383822589138305L // close together, somewhat sparse edges
                    , 2468959635154320336L  // close together near edge
                    , -8656168586251252363L // fairly distant, average connectivity, larger
                    , 8649022763586768876L  // distant, low connectivity, mid-size
                    , 8728421619592406161L  // distant, average connectivity, larger
                    , -1052520370731095974L // larger, high connectivity
                    , 2367228435557748823L  // small, distant, high connectivity
                    , 1577324789582274410L  // very distant, larger, high connectivity
                    , 4123341367544144242L  // extremely close, very linear
            };

    private static final long[] CORRECTNESS_SEEDS= new long[]
            { 6661338013407588541L  // lots of dead ends
                    , 7805163969130267793L  // one dead-end near PlanetX
                    , -8175053985118269957L // some dead-ends near PlanetX
                    , 4066957969576589441L  // dead ends if optimized
                    // some arbitrarily chosen seeds
                    , -4357902281793867410L
                    , 627623984062512473L
                    , 6831612833360485366L
                    , 1952601744907090123L
                    , -7754468029182729676L
                    , -8037518504153366330L
                    , 3358439255283285461L
                    , 7396777835070441604L
                    , 1852814601028238082L
                    , -2288709736544510491L
            };

    /** Append the prologue of the feedback file to sb with grader name g. */
    public static StringBuilder prologue(StringBuilder sb, String g) {
        sb.append("Hello, this is ").append(g).append(", grading your A8.\n\n");
        sb.append(
                "A8's grade is divided into correctness and optimality.\n" +
                        "We run your solution on various predetermined seeds, checking " +
                        "whether it is successful.\n" +
                        "Iff a solution succeeds, we compare your score in the " +
                        "rescue and return stages to our basic\nand optimized solution " +
                "scores, awarding points based on how your score compares.\n\n");
        sb.append(
                "The point breakdown is given in the grading table at the end of this file.\n" +
                        "During the rescue stage, a max of 20 seconds is allowed per map.\n" +
                "During the return stage, (15 + (number of nodes) / 50) seconds.").append("\n\n");
        sb.append("Optimality results are formatted as follows:\nif your solution " +
                "succeeds, we display your score versus [our basic score, our " +
                "optimized score].\n");
        return sb;
    }

    // NON-AMORTIZED BENCHMARK FUNCTIONS
    //    /** Return the low rescue stage optimality benchmark for the given seed. */
    //    private static int rescueLo(long seed) {
    //        return new GraderPlanetX(seed, new BasicSpaceship()).gradeRescue();
    //    }
    //    
    //    /** Return the high rescue stage optimality benchmark for the given seed. */
    //    private static int rescueHi(long seed) {
    //        return new GraderPlanetX(seed, new OptimizedSpaceship()).gradeRescue();
    //    }
    //
    //    /** Return the low return stage optimality benchmark for the given seed. */
    //    private static int returnLo(long seed) {
    //        return new GraderPlanetX(seed, new BasicSpaceship()).gradeReturn();
    //    }
    //
    //    /** Return the high return stage optimality benchmark for the given seed. */
    //    private static int returnHi(long seed) {
    //        return new GraderPlanetX(seed, new OptimizedSpaceship()).gradeReturn();
    //    }

    //AMORTIZED BENCHMARK FUNCTIONS
    private static HashMap<Long, Integer> rescueLoScores= new HashMap<>();
    private static HashMap<Long, Integer> rescueHiScores= new HashMap<>();
    private static HashMap<Long, Integer> returnLoScores= new HashMap<>();
    private static HashMap<Long, Integer> returnHiScores= new HashMap<>();
    static { // this is far from elegant; external file would be better
        rescueLoScores.put(-60111111L, 0);
        rescueLoScores.put(-333333L, 0);
        rescueLoScores.put(-9999L, 0);
        rescueLoScores.put(-3357383822589138305L, 0);
        rescueLoScores.put(5378428136332632818L, 0);
        rescueLoScores.put(2468959635154320336L, 0);
        rescueLoScores.put(8649022763586768876L, 0);

        rescueHiScores.put(-60111111L, 748);
        rescueHiScores.put(-333333L, 789);
        rescueHiScores.put(-9999L, 981);
        rescueHiScores.put(-3357383822589138305L, 1865);
        rescueHiScores.put(5378428136332632818L, 1912);
        rescueHiScores.put(2468959635154320336L, 683);
        rescueHiScores.put(8649022763586768876L, 349);

        returnLoScores.put(-60111111L, 0);
        returnLoScores.put(-3357383822589138305L, 3493);
        returnLoScores.put(2468959635154320336L, 3713);
        returnLoScores.put(-8656168586251252363L, 71575);
        returnLoScores.put(8649022763586768876L, 1390);
        returnLoScores.put(8728421619592406161L, 76771);
        returnLoScores.put(-1052520370731095974L, 51947);
        returnLoScores.put(2367228435557748823L, 13919);
        returnLoScores.put(1577324789582274410L, 40511);
        returnLoScores.put(4123341367544144242L, 3668);

        returnHiScores.put(-60111111L, 49603);
        returnHiScores.put(-3357383822589138305L, 150109);
        returnHiScores.put(2468959635154320336L, 832387);
        returnHiScores.put(-8656168586251252363L, 1566061);
        returnHiScores.put(8649022763586768876L, 563420);
        returnHiScores.put(8728421619592406161L, 796562);
        returnHiScores.put(-1052520370731095974L, 1489764);
        returnHiScores.put(2367228435557748823L, 245963);
        returnHiScores.put(1577324789582274410L, 1741408);
        returnHiScores.put(4123341367544144242L, 308998);
    }

    /** Return the low rescue stage optimality benchmark for the given seed. */
    private static int rescueLo(long seed) {
        return rescueLoScores.get(seed);
    }

    /** Return the high rescue stage optimality benchmark for the given seed. */
    private static int rescueHi(long seed) {
        return rescueHiScores.get(seed);
    }

    /** Return the low return stage optimality benchmark for the given seed. */
    private static int returnLo(long seed) {
        return returnLoScores.get(seed);
    }

    /** Return the high return stage optimality benchmark for the given seed. */
    private static int returnHi(long seed) {
        return returnHiScores.get(seed);
    }

    /** Return the points to deduct for a given student score, low and high
     * benchmark scores, and total seeds to run. */
    private static double deductOptimality(int student, int lo, int hi,
            int totalSeeds, double maxScore) {
        if (lo > student) return maxScore / totalSeeds;
        return maxScore / totalSeeds * (1 - ((double)(student - lo) / (hi - lo)));
    }

    /** Append the rescue stage correctness score to sb and g for the seed s,
     * assuming totalSeeds will eventually be run. */
    public static StringBuilder rescueCorrectness(StringBuilder sb, Grade g,
            long s, int totalSeeds) {
        GraderPlanetX student= new GraderPlanetX(s, new MySpaceship());
        long time= 20;
        student.gradeRescue(time);
        sb.append("number of nodes: ").append(student.getNumNodes()).append("\n");
        if (student.searchSucceeded()) {
            sb.append("Rescue succeeded!\n");
        } else if (student.isTimedOut()) {
            g.rescueC-= RESCUE_CORRECTNESS / totalSeeds;
            sb.append("[X] Rescue timed out after ").append(time).append(" seconds!\n");
        } else {
            g.rescueC-= RESCUE_CORRECTNESS / totalSeeds;
            sb.append("[X] Rescue failed!\n");
        }
        return sb;
    }

    /** Append the rescue stage optimality score to sb and g for the seed s,
     * assuming totalSeeds will eventually be run. */
    public static StringBuilder rescueOptimality(StringBuilder sb, Grade g,
            long s, int totalSeeds) {
        GraderPlanetX student= new GraderPlanetX(s, new MySpaceship());
        long time= 20;
        int studentScore= student.gradeRescue(time);
        sb.append("number of nodes: ").append(student.getNumNodes()).append("\n");
        if (student.searchSucceeded()) {
            int loScore= rescueLo(s);
            int hiScore= rescueHi(s);
            g.rescueO-= deductOptimality(studentScore, loScore, hiScore,
                    totalSeeds, RESCUE_OPTIMALITY);
            sb.append("Rescue score only: ").append(studentScore).append("        versus [")
            .append(loScore).append(", ").append(hiScore).append("]\n");
        } else if (student.isTimedOut()) {
            g.rescueO-= RESCUE_OPTIMALITY / totalSeeds;
            sb.append("[X] Rescue timed out after ").append(time).append(" seconds!\n");
        } else {
            g.rescueO-= RESCUE_OPTIMALITY / totalSeeds;
            sb.append("[X] Rescue failed!\n");
        }
        return sb;
    }

    /** Append the return stage correctness score to sb and g for the seed s,
     * assuming totalSeeds will eventually be run. */
    public static StringBuilder returnCorrectness(StringBuilder sb, Grade g,
            long s, int totalSeeds) {
        GraderPlanetX student= new GraderPlanetX(s, new MySpaceship());
        long time= 15 + student.getNumNodes() / 50;
        student.gradeReturn(time);
        if (student.rescueSucceeded()) {
            sb.append("Return succeeded!\n");
        } else if (student.isTimedOut()) {
            g.returnC-= RETURN_CORRECTNESS / totalSeeds;
            sb.append("[X] Return timed out after ").append(time).append(" seconds!\n");
        } else {
            g.returnC-= RETURN_CORRECTNESS / totalSeeds;
            sb.append("[X] Return failed!\n");
        }
        return sb;
    }

    /** Append the return stage optimality score to sb and g for the seed s,
     * assuming totalSeeds will eventually be run. */
    public static StringBuilder returnOptimality(StringBuilder sb, Grade g,
            long s, int totalSeeds) {
        GraderPlanetX student= new GraderPlanetX(s, new MySpaceship());
        long time= 15 + (student.getNumNodes() / 50);
        int studentScore= student.gradeReturn(time);
        sb.append("number of nodes: ").append(student.getNumNodes()).append("\n");
        if (student.rescueSucceeded()) {
            int loScore= returnLo(s);
            int hiScore= returnHi(s);
            g.returnO-= deductOptimality(studentScore, loScore, hiScore,
                    totalSeeds, RETURN_OPTIMALITY);
            sb.append("Return score only: ").append(studentScore).append("        versus [")
            .append(loScore).append(", ").append(hiScore).append("]\n");
        } else if (student.isTimedOut()) {
            g.returnO-= RETURN_OPTIMALITY / totalSeeds;
            sb.append("[X] Return timed out after ").append(time).append(" seconds!\n");
        } else {
            g.returnO-= RETURN_OPTIMALITY / totalSeeds;
            sb.append("[X] Return failed!\n");
        }
        return sb;
    }

    /** Return the NetID(s) of a CMS submission string, which is formatted
     * either as ".+" or "group_of_.+_.+". */
    private static String[] extractNetIDs(String cmsSubmissionName) {
        if (cmsSubmissionName.startsWith("group_of_")) {
            String p= cmsSubmissionName.substring(9); // length of group_of_
            return new String[]
                    { p.substring(0, p.indexOf('_'))
                            , p.substring(p.indexOf('_') + 1)
                    };
        }
        return new String[] { cmsSubmissionName };
    }

    /** Run the grading script for a given submission.
     * Precondition: argv[0] is the grader's name, and argv[1] is the
     * student(s)' NetID(s), either in the format ".+" or "group_of_.+_.+". */
    public static void main(String[] argv) {
        if (argv.length < 2) {
            System.err.println("usage: a8Grader GRADER SUBMISSION");
            System.exit(1);
            return;
        }

        PrintStream stdout= System.out;
        PrintStream stderr= System.err;

        // watch stdout/stderr for any writes
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                printed= true;
            }
        }));
        System.setErr(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                printed= true;
            }
        }));

        PrintWriter pw;
        String path= "Submissions/" + argv[1] + "/" + argv[1] + "_feedback.txt";
        File file= new File(path);
        if (!file.canWrite() && (file.isDirectory() || !file.getParentFile().exists())) {
            stderr.println("fatal error: cannot write to feedback file at\n" + path);
            System.exit(1);
            return;
        }

        StringBuilder sb= new StringBuilder(10000);
        Grade g= new Grade();

        prologue(sb, argv[0]);
        sb.append("\n\nTesting correctness");
        for (long seed : CORRECTNESS_SEEDS) {
            sb.append("\nSeed: ").append(seed).append("; ");
            rescueCorrectness(sb, g, seed, CORRECTNESS_SEEDS.length);
            returnCorrectness(sb, g, seed, CORRECTNESS_SEEDS.length);
        }

        sb.append("\n\nTesting rescue stage optimality, only (return stage is NOT run)");
        for (long seed : RESCUE_OPTIMALITY_SEEDS) {
            sb.append("\nSeed: ").append(seed).append("; ");
            rescueOptimality(sb, g, seed, RESCUE_OPTIMALITY_SEEDS.length);
        }

        sb.append("\n\nTesting return stage optimality, only (rescue stage is NOT run)");
        for (long seed : RETURN_OPTIMALITY_SEEDS) {
            sb.append("\nSeed: ").append(seed).append("; ");
            returnOptimality(sb, g, seed, RETURN_OPTIMALITY_SEEDS.length);
        }

        int deductions= 0;
        if (printed) {
            sb.append("\n\nDeduction )").append(PRINT_PENALTY).append(
                    ". Your solution printed to console. " +
                            "We specifically said not to do this in the handout:\n\n" +
                            "    [...] Don't leave any traces of debugging code, such as " +
                    "print statements.\n\n");
            deductions+= PRINT_PENALTY;
        }

        sb.append("\n\nPoints:\n").append("Rescue stage:\n").append(
                "    Correctness: ").append(g.rescueCorrectness()).append(" of ")
        .append(RESCUE_CORRECTNESS).append("\n" +
                "    Optimality:  ").append(g.rescueOptimality()).append(" of ")
        .append(RESCUE_OPTIMALITY).append("\nReturn stage:\n").append(
                "    Correctness: ").append(g.returnCorrectness()).append(" of ")
        .append(RETURN_CORRECTNESS).append("\n" +
                "    Optimality:  ").append(g.returnOptimality()).append(" of ")
        .append(RETURN_OPTIMALITY).append("\n\n");
        sb.append("Total points: ").append(g.totalGrade()).append("\n");

        sb.append("DEDUCTIONS: ").append(deductions).append("\n");
        int totalGrade= Math.max(g.truncTotalGrade() - deductions, 0);
        sb.append("TOTAL GRADE: ").append(totalGrade).append("\n");

        try {
            pw= new PrintWriter(path);
        } catch (FileNotFoundException e) {
            stderr.println("fatal error: could not find feedback file at\n" + path);
            System.exit(1);
            return;
        }
        pw.print(sb);
        pw.flush();
        pw.close();

        /* output NetID(s) and grades, to be caught by an external script for
         * the grade csv file */
        for (String s : extractNetIDs(argv[1])) {
            stdout.printf("%s,%d\n", s, totalGrade);
        }
    }

    /** An instance maintains the current grade of a solution. */
    private static class Grade {
        public double rescueC; // rescue correctness
        public double rescueO; // rescue optimality
        public double returnC; // return correctness
        public double returnO; // return optimality

        public Grade() {
            rescueC= RESCUE_CORRECTNESS;
            rescueO= RESCUE_OPTIMALITY;
            returnC= RETURN_CORRECTNESS;
            returnO= RETURN_OPTIMALITY;
        }

        /** Return the rescue correctness grade with two decimal places. */
        public String rescueCorrectness() {
            return String.format("%.2f", Math.min(RESCUE_CORRECTNESS,
                    Math.max(rescueC, 0)));
        }

        /** Return the rescue optimality grade with two decimal places. */
        public String rescueOptimality() {
            return String.format("%.2f", Math.min(RESCUE_OPTIMALITY,
                    Math.max(rescueO, 0)));
        }

        /** Return the rescue correctness grade with two decimal places. */
        public String returnCorrectness() {
            return String.format("%.2f", Math.min(RETURN_CORRECTNESS,
                    Math.max(returnC, 0)));
        }

        /** Return the return optimality grade with two decimal places. */
        public String returnOptimality() {
            return String.format("%.2f", Math.min(RETURN_OPTIMALITY,
                    Math.max(returnO, 0)));
        }

        /** Return the truncated total grade. */
        public int truncTotalGrade() {
            return Math.min(100,
                    (int)Math.max(0, Math.min(RESCUE_CORRECTNESS, rescueC)
                            + Math.min(RESCUE_OPTIMALITY, rescueO)
                            + Math.min(RETURN_CORRECTNESS, returnC)
                            + Math.min(RETURN_OPTIMALITY, returnO) + 0.005
                            )
                    );
        }

        /** Return the total grade with two decimal places. */
        public String totalGrade() {
            return String.format("%.2f", Math.min(100,
                    Math.max(0, Math.min(RESCUE_CORRECTNESS, rescueC)
                            + Math.min(RESCUE_OPTIMALITY, rescueO)
                            + Math.min(RETURN_CORRECTNESS, returnC)
                            + Math.min(RETURN_OPTIMALITY, returnO)
                            )
                    ));
        }
    }
}
