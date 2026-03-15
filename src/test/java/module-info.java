module SmallCSV.test {

    requires SmallCSV;
    requires org.junit.jupiter;
    requires org.junit.jupiter.api;

    opens io.github.sekelenao.smallcsv.test to org.junit.platform.commons, SmallCSV;

}