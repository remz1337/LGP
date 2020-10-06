package nz.co.jedsimson.lgp.core.environment.dataset

//import com.opencsv.CSVReader
import nz.co.jedsimson.lgp.core.environment.ComponentLoaderBuilder
import nz.co.jedsimson.lgp.core.environment.MemoizedComponentProvider
import java.io.FileReader
import java.io.Reader
import nz.co.jedsimson.lgp.core.modules.ModuleInformation
import java.util.ArrayList

import java.io.BufferedReader

// These type aliases help to make the code look nicer.
typealias TxtHeader = String
typealias TxtRow = String

/**
 * Exception given when a TXT file that does not match the criteria the system expects
 * is given to a [TxtDatasetLoader] instance.
 */
class InvalidTxtFileException(message: String) : Exception(message)

/**
 * Loads a collection of samples and their target values from a TXT file.
 *
 * @param TData Type of the features in the samples.
 * @param TTarget Type of the outputs in the dataset.
 * @property reader A reader that will provide the contents of a TXT file.
 * @property featureParseFunction A function to parse the features of each row in the TXT file.
 * @property targetParseFunction A function to parse the target of each row in the TXT file.
 */
class TxtDatasetLoader<TData, TTarget : Target<TData>> constructor(
        val reader: Reader,
        val txtParseFunction: (List<String>) -> Dataset<TData, TTarget>
        //val featureParseFunction: (TxtHeader, TxtRow) -> Sample<TData>,
        //val targetParseFunction: (TxtHeader, TxtRow) -> TTarget
) : DatasetLoader<TData, TTarget> {

    private constructor(builder: Builder<TData, TTarget>)
        : this(builder.reader, builder.txtParseFunction)

    private val datasetProvider = MemoizedComponentProvider("Dataset") { this.initialiseDataset() }

    /**
     * Builds an instance of [TxtDatasetLoader].
     *
     * @param UData the type that the [TxtDatasetLoader] will load features as.
     * @param UData the type that the [TxtDatasetLoader] will load features as.
     */
    class Builder<UData, UTarget : Target<UData>> : ComponentLoaderBuilder<TxtDatasetLoader<UData, UTarget>> {

        lateinit var reader: Reader
        lateinit var txtParseFunction: (List<String>) -> Dataset<UData, UTarget>
        //lateinit var featureParseFunction: (TxtHeader, TxtRow) -> Sample<UData>
        //lateinit var targetParseFunction: (TxtHeader, TxtRow) -> UTarget

        /**
         * Sets the filename of the TXT file to load the data set from.
         *
         * A reader will be automatically created for the file with the name given.
         */
        fun filename(name: String) = apply {
            this.reader = FileReader(name)
        }

        /**
         * Sets the reader that provides a TXT files contents.
         */
        fun reader(reader: Reader) = apply {
            this.reader = reader
        }

        fun txtParseFunction(function: (List<String>) -> Dataset<UData, UTarget>) = apply {
            this.txtParseFunction = function
        }

        /**
         * Sets the function to use when parsing features from the data set file.
         */
        /*fun featureParseFunction(function: (TxtHeader, TxtRow) -> Sample<UData>) = apply {
            this.featureParseFunction = function
        }*/

        /**
         * Sets the function to use when parsing target values from the data set file.
         */
        /*fun targetParseFunction(function: (TxtHeader, TxtRow) -> UTarget) = apply {
            this.targetParseFunction = function
        }*/

        /**
         * Builds the instance with the given configuration information.
         */
        override fun build(): TxtDatasetLoader<UData, UTarget> {
            return TxtDatasetLoader(this)
        }
    }

    /**
     * Loads a data set from the TXT file specified when the loader was built.
     *
     * @throws [java.io.IOException] when the file given does not exist.
     * @returns a data set containing values parsed appropriately.
     */
    override fun load(): Dataset<TData, TTarget> {
        return this.datasetProvider.component
    }

    private fun initialiseDataset(): Dataset<TData, TTarget> {

        val fileReader = this.reader

        val bufferedReader = BufferedReader(fileReader)
        //val lines: MutableList<String> = java.util.ArrayList<String>()
        //var line: String = ""

        val linesRead = bufferedReader.readLines()  // defaults to UTF-8
        //while (bufferedReader.readLine().also { line = it } != null) {
        /*while (bufferedReader.readLine()) {
            lines.add(line)
        }*/

        bufferedReader.close()

        //val lines=linesRead.toMutableList()


        //val reader2 = FileReader(this.reader)
        //val lines: MutableList<Array<String>> = reader2

        // Make sure there is data before we continue. There should be at least two lines in the file
        // (a header and one row of data). This check will let through a file with 2 data rows, but
        // there is not much that can be done -- plus things will probably break down later on...
        if (linesRead.size < 2)
            throw InvalidTxtFileException("TXT file should have a header row and one or more data rows.")

        // Assumes the header is in the first row (a reasonable assumption with TXT files).
        /*val header = lines.removeAt(0)

        // Parse features and target values individually.
        val features = lines.map { line ->
            this.featureParseFunction(header, line)
        }

        val targets = lines.map { line ->
            this.targetParseFunction(header, line)
        }*/

        val dataset = this.txtParseFunction(linesRead)

        return dataset
        //return Dataset(features, targets)
    }

    override val information = ModuleInformation(
            description = "A loader that can load data sets from TXT files."
    )
}