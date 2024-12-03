import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import sys
import matplotlib
from matplotlib.ticker import FormatStrFormatter

matplotlib.rcParams['font.family'] = 'Times New Roman'
matplotlib.rcParams['pdf.fonttype'] = '42'
matplotlib.rcParams['ps.fonttype'] = '42'

plt.rc('text', usetex=True)

ticks_font_size = 25


files = [
    './results_rq1_csv/hdd_ddmin_c.csv',
    './results_rq1_csv/perses_ddmin_c.csv',
    './results_rq1_csv/hdd_ddmin_xml.csv',
    './results_rq1_csv/perses_ddmin_xml.csv',
]

def c_spearman_by_benchmark():
    dfs = []
    for i in range(0, 2):
        file = files[i]
        df = pd.read_csv(file)
        df = df[~df['Benchmark'].str.startswith('rust')]
        # df = df['spearman_corr']
        average_spearman_corr = df.groupby('Benchmark')['spearman_corr'].mean().reset_index()
        print(average_spearman_corr)
        dfs.append(average_spearman_corr['spearman_corr'])

    combined_data = pd.concat(dfs, axis=1)
    combined_data.columns = [r'HDD\textsubscript{d}', r'Perses\textsubscript{d}']
    plt.figure(figsize=(6, 4))
    box_plot = sns.boxplot(data=combined_data, width=0.4, boxprops=dict(facecolor='none'), showfliers=False)
    sns.stripplot(data=combined_data, palette='Set2', size=6, alpha=0.9, jitter=True)
    mean_values = combined_data.mean()
    std_values = combined_data.std()

    # Add a red dashed line at y=1.0
    plt.axhline(y=0.0, linestyle='--', color='gray', linewidth=2.0)
    print(plt.gca().get_ylim())
    plt.ylim(bottom=-0.80, top=0.5)
    plt.xticks(fontsize=ticks_font_size)
    plt.yticks(fontsize=ticks_font_size)
    plt.gca().yaxis.set_major_formatter(FormatStrFormatter('%.1f'))
    # show mean and std on top of the box plot
    
    for i in range(len(mean_values)):
        plt.text(i, 0.39, f'$\mu$ = {mean_values[i]:.2f}', ha='center', va='bottom', fontsize=18, color='black')
        plt.text(i, 0.30, f'$\sigma$ = {std_values[i]:.2f}', ha='center', va='bottom', fontsize=18, color='black')

    plt.show()
    # plt.savefig('correlation-ddmin-c-spearman_by_benchmark.pdf', format='pdf', bbox_inches='tight', dpi=300)


def xml_spearman_by_benchmark():
    dfs = []
    for i in range(2, 4):
        file = files[i]
        df = pd.read_csv(file)
        df = df[~df['Benchmark'].str.startswith('rust')]
        average_spearman_corr = df.groupby('Benchmark')['spearman_corr'].mean().reset_index()

        print(average_spearman_corr)
        dfs.append(average_spearman_corr['spearman_corr'])


    combined_data = pd.concat(dfs, axis=1)
    combined_data.columns = [r'HDD\textsubscript{d}', r'Perses\textsubscript{d}']
    plt.figure(figsize=(6, 4))
    box_plot = sns.boxplot(data=combined_data, width=0.4, boxprops=dict(facecolor='none'), showfliers=False)
    sns.stripplot(data=combined_data, palette='Set2', size=6, alpha=0.9, jitter=True)
    mean_values = combined_data.mean()
    std_values = combined_data.std()

    # Add a red dashed line at y=1.0
    plt.axhline(y=0.0, linestyle='--', color='gray', linewidth=2.0)

    print(plt.gca().get_ylim())
    plt.ylim(bottom=-1, top=1)
    plt.xticks(fontsize=ticks_font_size)
    plt.yticks(fontsize=ticks_font_size)
    plt.gca().yaxis.set_major_formatter(FormatStrFormatter('%.1f'))
    # show mean and std on top of the box plot
    
    for i in range(len(mean_values)):
        plt.text(i, 0.50, f'$\mu$ = {mean_values[i]:.2f}', ha='center', va='bottom', fontsize=18, color='black')
        plt.text(i, 0.36, f'$\sigma$ = {std_values[i]:.2f}', ha='center', va='bottom', fontsize=18, color='black')

    plt.show()
    # plt.savefig('correlation-ddmin-xml-spearman_by_benchmark.pdf', format='pdf', bbox_inches='tight', dpi=300)


if __name__ == '__main__':
    c_spearman_by_benchmark()
    xml_spearman_by_benchmark()