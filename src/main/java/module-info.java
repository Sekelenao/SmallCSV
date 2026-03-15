module SmallCSV {

    // API
    exports io.github.sekelenao.smallcsv.api;

    // Exceptions
    exports io.github.sekelenao.smallcsv.api.exception;

    // Exports for tests
    exports io.github.sekelenao.smallcsv.internal to SmallCSV.test;

    // Opens for tests
    opens io.github.sekelenao.smallcsv.api to SmallCSV.test;
    opens io.github.sekelenao.smallcsv.internal to SmallCSV.test;

}