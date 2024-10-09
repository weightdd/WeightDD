import unittest
import tempfile
from shutil import rmtree
from run_benchmark_global_cache import *


class BenchmarkTest(unittest.TestCase):

    tmp_dir = None
    subjects = 'benchmark_for_testing_scripts'

    def setUp(self) -> None:
        self.tmp_dir = tempfile.mkdtemp()

    def tearDown(self) -> None:
        rmtree(self.tmp_dir)

    def test_extract_info_properties(self):
        src, script = extract_info_properties(subject_name=self.subjects)
        self.assertEqual(os.path.basename(src), 't.c')
        self.assertEqual(os.path.basename(script), 'r.sh')

    def test_filename_generator(self):
        # subject basename should be extracted from the path
        subject = '../dummy_folder/dummy-subject'
        reducer, iteration, timemark = 'hdd', 0, '20200202'

        report, log = filename_generator(subject, reducer, iteration, timemark)
        self.assertEqual(report, 'tmp_dummy-subject_hdd_20200202_itr0.json')

    def test_system_with_cache_enabled(self):
        cache_file_path = os.path.join(self.tmp_dir, 'lookup_table.json')
        # execution which creates a cache file
        parameter = Parameter(
            benchmark_target=[self.subjects],
            reducers=["perses"],
            show_subprocess=False,
            iterations=1,
            list_reducers=False,
            memory_profiler=False,
            output_dir=self.tmp_dir,
            enable_cache=True,
            cache_file_path=cache_file_path
        )
        report = main(parameter)
        self.assertEqual(report["Token_remaining"], 11)

        self.assertTrue(os.path.isfile(cache_file_path))
        # execution which uses the cache file
        parameter = Parameter(
            benchmark_target=[self.subjects],
            reducers=["perses"],
            show_subprocess=False,
            iterations=1,
            list_reducers=False,
            memory_profiler=False,
            output_dir=self.tmp_dir,
            enable_cache=True,
            cache_file_path=cache_file_path
        )
        report = main(parameter)
        self.assertEqual(report["Token_remaining"], 11)

    def test_system_with_cache_disabled(self):
        parameter = Parameter(
            benchmark_target=[self.subjects],
            reducers=["perses"],
            show_subprocess=False,
            iterations=1,
            list_reducers=False,
            memory_profiler=False,
            output_dir=self.tmp_dir,
            enable_cache=False,
            cache_file_path=os.path.join(self.tmp_dir, 'lookup_table.json')
        )
        report = main(parameter)
        self.assertEqual(report["Token_remaining"], 11)


if __name__ == '__main__':
    unittest.main()
