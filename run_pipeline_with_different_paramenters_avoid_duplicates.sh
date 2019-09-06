#!/bin/bash

#run e.g. 
# bash run_pipeline_with_different_paramenters_avoid_duplicates.sh app_dirs/gender_equality_part1/ \
#"2" "2" "3 5" "30" "4" "5000" beta_writer/dist/beta_writer_WITHOUT_DUPLICATES_NEW.jar -Xmx16g "gender_equality_part1"


#arguments:
#1) directory containing A++ files
#2) String with white-space separated numbers of chapters
#3) String with white-space separated numbers of sections
#4) String with white-space separated numbers of papers
#5) String with white-space separated values for max_df
#6) String with white-space separated values for min_df
#7) String with white-space separated values for feats
#8) path of beta-writer jar file
#9) Value for the memory usage (-Xmx...)
#10) keyword (used to create the output directories: gen_+keyword+parameters)

#output directories for each value in the white-space separated strings:
#e.g. gen_interventional_radiology_and_broad_and_narrow_c-5_s-5_p-100_maxdf-5760_mindf-650_feats-5000/


appdir=$1
num_chaps=$2 #"4"
num_secs=$3 #"2"
num_ps=$4 #"10"

max_df=$5
min_df=$6
num_feats=$7
XMX=$9 #e.g. -Xmx9g
kw=${10}

beta_writer_jar=$8
echo "beta writer jar: "$beta_writer_jar

#kw_rev="$(cut -d'/' -f2 <<< "$appdir" | rev)"
#kw="$(echo $kw_rev | rev)"
echo "keyword: "$kw

for maxdf in $max_df; do
  for mindf in $min_df; do
    for nfeats in $num_feats; do
      for ns in $num_secs; do
        for np in $num_ps; do
          for nc in $num_chaps; do
            shared_dir="gen_"$kw
            output_dir="gen_"$kw"_c-"$nc"_s-"$ns"_p-"$np"_maxdf-"$maxdf"_mindf-"$mindf"_feats-"$nfeats"/"
            #check if output_dir already exists
            if [ -d $output_dir ]; then
              echo "Results already exist: "$output_dir;
            else
              echo "Running with parameters: nc: "$nc" - ns: "$ns" - np: "$np" - maxdf: "$maxdf" - mindf: "$mindf" -  feats: "$nfeats" - output_dir: "$output_dir" jar: "$beta_writer_jar;
              sh pipeline_parameters_avoid_duplicates.sh $appdir $shared_dir $nc $ns $np $maxdf $mindf $nfeats $beta_writer_jar $XMX $output_dir
            fi;
          done;
        done;
      done;
    done;
  done;
done;
