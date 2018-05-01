# SC2-SommerCamp

## Install linux

Install wine and download [Starcraft 2 Installer][sc2installer], then install the game with an optional wine prefix.

```bash
  > WINEPREFIX=/home/$USER/SC2/bnet wine ~/Downloads/StarCraft-II-Setup.exe
```

Install windows corefonts
```bash
  > WINEPREFIX=/home/$USER/SC2/bnet winetricks corefonts fontsmooth=rgb
```

Copy the maps into the game folder:
```bash
  > cp -r maps/* SC2/StarCraft\ II/maps/
```

Starting the game for AI client to connect to:
```bash
  > cd /home/$USER/SC2/SC2/StarCraft\ II/Support
  > WINEPREFIX=/home/vulder/SC2/bnet wine ../Versions/Base64469/SC2.exe -listen 127.0.0.1 -port 8167 -displayMode 1 -windowwidth 1024 -windowheight 768 -windowx 100 -windowy 200
```

[sc2installer]: https://www.battle.net/download/getInstallerForGame?os=win&locale=enUS&version=LIVE&gameProgram=STARCRAFT_2
