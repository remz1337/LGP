package lgp.core.environment.dataset

import com.opencsv.CSVReader
import lgp.core.environment.ComponentLoaderBuilder
import lgp.core.modules.ModuleInformation
import java.io.FileReader
import java.io.Reader

//  These helper classes aren't strictly necessary, but they help to make the
// types used by the loader look a little bit nicer.

/**
 * An instance in a [CsvDataset].
 *
 * @param T the type of the attributes.
 * @property data a collection of attributes.
 * @suppress
 */
class Row<out T>(data: List<Attribute<T>>) : Instance<T>(data)

/**
 * A data set from a CSV file.
 *
 * @suppress
 */
class CsvDataset<T>(rows: List<Row<T>>) : Dataset<T>(rows)

/**
 * Loads a collection of instances from a CSV file.
 *
 * @param T Type of the attributes in the instances.
 * @property filename CSV file to load instances from.
 * @property parseFunction Function to parse each attribute in the file.
 */
class CsvDatasetLoader<T> constructor(
        val reader: Reader,
        val parseFunction: (String) -> T,
        val labels: List<String> = listOf()
) : DatasetLoader<T> {

    private constructor(builder: Builder<T>) : this(builder.reader, builder.parseFunction, builder.labels)

    /**
     * Builds an instance of [CsvDatasetLoader].
     *
     * @param U the type that the [CsvDatasetLoader] will load attributes as.
     */
    class Builder<U> : ComponentLoaderBuilder<CsvDatasetLoader<U>> {

        lateinit var reader: Reader
        lateinit var parseFunction: (String) -> U
        var labels: List<String> = listOf()

        /**
         * Sets the filename of the CSV file to load the data set from.
         */
        fun filename(name: String): Builder<U> {
            this.reader = FileReader(name)

            return this
        }

        fun reader(reader: Reader): Builder<U> {
            this.reader = reader

            return this
        }

        /**
         * Sets the function to use when parsing attributes from the data set file.
         */
        fun parseFunction(function: (String) -> U): Builder<U> {
            this.parseFunction = function

            return this
        }

        fun labels(labels: List<String>): Builder<U> {
            this.labels = labels

            return this
        }

        /**
         * Builds the instance with the given configuration information.
         */
        override fun build(): CsvDatasetLoader<U> {
            return CsvDatasetLoader(this)
        }
    }

    /**
     * Loads a data set from the CSV file specified when the loader was built.
     *
     * @throws [java.io.IOException] when the file given does not exist.
     * @returns a data set containing values parsed appropriately.
     */
    override fun load(): CsvDataset<T> {
        val reader: CSVReader = CSVReader(this.reader)
        val lines: MutableList<Array<String>> = reader.readAll()

        // Assumes the header is in the first row (a reasonable assumption with CSV files).
        val header = lines.removeAt(0)

        val rows = lines.map { line -> Row(this.readAttributesFromRow(line, header)) }

        return CsvDataset(rows)
    }

    private fun readAttributesFromRow(line: Array<String>, header: Array<String>): List<Attribute<T>> {
        return line.mapIndexed { index, value ->
            when (value) {
                in this.labels -> NominalAttribute(header[index], this.parseFunction(value), this.labels)
                else -> Attribute(header[index], this.parseFunction(value))
            }
        }
    }

    override val information = ModuleInformation(
        description = "A loader than can load data sets from CSV files."
    )
}