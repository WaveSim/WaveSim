# WaveSim
Live simulation of waves and/or tsunamis

## What looks WaveSim like?
![Preview of running WaveSim][preview]

## Import in eclipse

```
mkdir wv
cd wv
git clone https://github.com/jzy3d/jzy3d-api.git
git clone https://github.com/WaveSim/WaveSim.git
# Make sure you have Eclipse capable to load Maven projects...
wget ftp://ftp.unidata.ucar.edu/pub/netcdf-java/v4.5/netcdfAll-4.5.jar 
# Or any other new netCDF .jar-lib from http://www.unidata.ucar.edu/software/thredds/current/netcdf-java/documentation.htm
```

A photo-(screenshots)-documentation and a simple guide for those who want to extend (or ...) WaveSim in eclipse are given in the following.
(Probably won't work on Macs. Some problem with JOGL or other libs...)

1. Import existing Maven project (jzy3d)
![Import existing Maven project (jzy3d)][s1]
2. Create new empty java-project e.g. named "WaveSim"
3. Right click on project "WaveSim" -> Import -> General -> File System
![Import -> General -> File System][s3]
4. Go to your WaveSim-git-directory and select the folders "src" and "res"
![select the folders "src" and "res"][s4]
5. Right click on project "WaveSim" -> Build Path -> Configure Build Path
6. In tab "Projects" add all jzy3d projects
![In tab "Projects" add all jzy3d projects][s6]
7. In tab Libraries  add external jar: netCDF library.
![In tab Libraries  add external jar: netCDF library.][s7]
8. Start the simulation in Main.java or create appropriate jar-file.
![Start the simulation in Main.java or create appropriate jar-file.][s8]
9. Happy painting!
![Happy painting!][s9]



[preview]: https://raw.githubusercontent.com/WaveSim/InstallGuide/master/WaveSimPreView.png
[s1]: https://raw.githubusercontent.com/WaveSim/InstallGuide/master/screenshot-1.png
[s3]: https://raw.githubusercontent.com/WaveSim/InstallGuide/master/screenshot-3.png
[s4]: https://raw.githubusercontent.com/WaveSim/InstallGuide/master/screenshot-4.png
[s6]: https://raw.githubusercontent.com/WaveSim/InstallGuide/master/screenshot-6.png
[s7]: https://raw.githubusercontent.com/WaveSim/InstallGuide/master/screenshot-7.png
[s8]: https://raw.githubusercontent.com/WaveSim/InstallGuide/master/screenshot-8.png
[s9]: https://raw.githubusercontent.com/WaveSim/InstallGuide/master/screenshot-9.png
