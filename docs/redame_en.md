<div align="center">
    <h1 align="center">
      
Programmable Hatches Mod

[![](https://github.com/reobf/Programmable-Hatches-Mod/actions/workflows/build-and-test.yml/badge.svg)](https://github.com/reobf/Programmable-Hatches-Mod/actions/workflows/build-and-test.yml)
</div>

## Introduction

This project is a community MOD for the GT New Horizons modpack, adding a few blocks and multi-block structures that can help players more easily achieve automation in their gameplay.

Please note, this MOD is not an official part of the GTNH Mod Pack. When discussing this MOD, please be mindful of the context.

If you encounter any bugs or other issues while playing with this MOD, please submit an issue, and if necessary, provide a method to reproduce the issue along with any relevant error logs.

## MOD Installation Guidelines

### Downloading and Versioning the MOD

Please download this MOD from the [Releases](https://github.com/reobf/Programmable-Hatches-Mod/releases) section on the Github page.

Please note:

This MOD currently supports single-file compatibility with GTNH modpack versions 2.6.0 and 2.6.1. For version 2.7.0, please download the version with the gtnh270 suffix. It does not support GTNH modpack versions 2.5.0 and below.

If the latest version of the MOD cannot run on supported modpack versions, please raise an issue.

Additionally, support for version 2.5.1 has been discontinued. v0.0.18p28 is the last version explicitly supporting 2.5.1. While later versions may work on 2.5.1, in principle, no longer will we address incompatibility issues specific to 2.5.1.

### Regarding MOD Language

After installing this MOD in the game, it will write corresponding translation entries into the GregTech.lang file based on your current game language, and these entries cannot be automatically removed after being written.

Therefore, you need to set up your game language beforehand before installing this MOD.

If you want to change the language after running the MOD and have enabled `UseThisFileAsLanguageFile=true`, you will need to delete GregTech.lang, or restore a backup of GregTech.lang from before the MOD was installed (if available).

## Content and Features

### Programmable Hatches

The primary function of this MOD is to facilitate automation in the game, as the virtual circuit boards in GTNH often make AE-managed automation cumbersome.

For this purpose, the MOD provides programmer overlay plates and various voltage level and capacity programmable dual-input hatches. These features are similar to the input bus and assembly functions built into GTNH but come with the following characteristics.

#### Change Circuit Board Settings

<div align="center">
  
![pic1](docs/1.png)

`Circuit Overlay Plate`

</div>

By applying the programmer overlay plate to the original GTNH input bus, the input bus will recognize the programmer circuit within the bus and set the virtual programming circuit of the bus to match the programmer circuit, consuming the programmer circuit in the process.

In addition to the original input bus, single-block small machines in GT can also use this overlay plate to quickly change the virtual circuit board.

At the same time, the dual-input hatches provided by this MOD do not require this overlay plate, as they come with built-in circuit board modification capabilities.

#### Creating Programmer Circuits

This MOD sets different methods for creating programmable circuits at each voltage stage. At the beginning, you can use a forging hammer to create programmer circuits.

<div align="center">
  
![pic2](docs/2.png)

`Forging Hammer to Create Programmer Circuits`

</div>

Besides, you can use the programmer circuit provider to conveniently and quickly synthesize programmer circuits. This provider needs to be connected to an AE network and requires a synthesis processor to function. The synthesis speed depends on the number of parallel synthesis processors you have.

<div align="center">
  
![pic3](docs/3.png)

`Circuit Provider T1`

</div>

In addition to the T1 level provider, there are more levels of providers, as well as pre-order circuit providers that come with templates and do not require items to be placed inside. For details, search in NEI and check their Tooltips.

Apart from circuit boards, other items such as catalysts, which are not consumed in recipes, can also be made into programmer circuits. If you need both virtual circuits and catalysts, you can use the multi-circuit slot hatch provided by this MOD.

#### How to Program Circuits into Templates

When writing AE templates, you can carry the programmer toolbox to program templates. The programmer toolbox can switch modes by holding Shift + Right-click. The functions of each mode are as follows:

* Mode 1: Disables the programmer toolbox, preventing any functionality when carrying the programmer toolbox.
* Mode 2: Enables automatic programming without item consumption. In this mode, placing the toolbox in your inventory will allow the use of NEI to transcribe recipes, converting all non-consumed items into programmer circuits and programming them into the template.
* Mode 3: Similar to Mode 2, but if the recipe does not require any non-consumed items, it will generate a programmer circuit that resets the circuit board to zero and programs it into the template.

### Programmable Dual-Input Hatch

This hatch works similarly to the assembly unit in-game, providing the required items and fluids to multi-block machines from a single block, while also supporting circuit programming. This hatch comes in multiple voltage level versions and upgraded buffer versions.

Based on its usage and cost, the Programmable Dual-Input Hatch has the following versions:

* **Programmable Dual-Input Hatch**: No buffer, with only one fluid input slot and item slot. The higher the voltage level, the more items and fluids it can input.
* **Programmable Multi-Fluid Dual-Input Hatch**: Compared to the non-multi-fluid version, this hatch supports more types of fluid inputs. The number of fluids it can support depends on the voltage level.
* **Programmable Buffered Dual-Input Hatch**: Compared to the non-buffered version, this hatch can cache items, allowing multi-block machines to prioritize reading items and fluids from the cache for recipe synthesis. The cache is typically four times the maximum input amount, making the Buffered Dual-Input Hatch beneficial for maximizing the parallel processing of multi-block machines.
* **Programmable Buffered Multi-Fluid Dual-Input Hatch**: Compared to the non-multi-fluid buffered version, this hatch can input multiple fluids.
* **Advanced Programmable Buffered Multi-Fluid Dual-Input Hatch**: Compared to the non-advanced buffered multi-fluid version, this hatch can store more types of caches, with different cache hatches isolated from each other, meaning items and fluids cannot be cross-referenced between caches. Using the advanced version of the Dual-Input Hatch helps solve the problem of machine parallelism not being effectively utilized due to order switching.

Here, we demonstrate using the Luv-level Advanced Programmable Buffered Multi-Fluid Input Hatch.

<div align="center">
  
![pic4](docs/4.png)

`Advanced Programmable Buffered Multi-Fluid Dual-Input Hatch Luv`

</div>

* **Position 1**: Item input interface.
* **Position 2**: Fluid input interface.

The number of items that can be stored at Position 1 is based on the voltage level setting. When sending items from an ME Interface, the maximum stack limit does not apply, for example, an Luv-level Advanced Programmable Buffered Multi-Fluid Input Hatch can stack up to 512 items per slot, while an EV-level can only stack 128 items.

The storage capacity for fluids at Position 2 is set similarly to Position 1. However, it's important to note that non-buffered versions of the Dual-Input Hatch can only input one type of fluid, whereas buffered versions can store more types of fluids as the voltage level increases. For instance, an Luv Buffered Multi-Fluid Dual-Input Hatch can input up to five types of fluids simultaneously, with each type storing up to 64,000L, and the total fluid input capacity for the advanced Luv Buffered Dual-Input Hatch remains unchanged but increases to 512,000L.

* **Position 3**: Cache interface

If you are using a buffered Dual-Input Hatch, items or fluids at Positions 1 and 2 will be transferred to the cache after 1 tick. At this point, the multi-block structure will prioritize reading items and fluids from the 0 cache position. The internal cache has four times the capacity marked externally. 

It's worth noting that although players often find the cache capacity to be very large, processing extremely large orders can still result in stuck orders because the external cache acceptance capacity is only a quarter of the internal cache capacity. Therefore, when programming templates, ensure that your fluid quantity does not exceed the maximum acceptable value of the input hatch, and pay attention to enabling the blocking mode of the ME Interface. If items inside the machine get mixed up, use the ME Suction tool from this MOD to return items to the AE network.

* **Position 4**: Adjustable settings for the Advanced Programmable Buffered Dual-Input Hatch

Here, you can:
> Set whether to use the programmable circuit mode.
> Set whether to lock fluids to a single slot at Position 2. Under normal circumstances, the fluid tank of this Dual-Input Hatch is like the quadruple input hatch in GTNH, where a single type of fluid can only be stored in one slot regardless of the quantity sent by AE. After unlocking, a single type of fluid can be stored across multiple slots during distribution.
> Set whether to enable input filtering mode, which works similarly to the input filtering mode of regular input buses, meaning that items not used by any recipe of the machine will not be allowed to enter.
> Input isolation mode cannot be switched.

* **Position 5**

Since items placed in the buffer input panel quickly enter the cache, manually placing items for synthesis is almost impossible, and you cannot place several groups of items in one slot at once. Therefore, you can turn off the power at Position 5, then use the insertion feature to manually place items on the input panel, and then turn on the power to move the items into the cache.

#### Other Types of Dual-Input Hatches

To address the issue of large single recipes in later stages, we added the Superfluid Dual-Input Hatch, capable of inputting 10,000,000L * 24 fluids.

We also added programmable pattern buses and assemblies, as well as corresponding mirror hatches that can use programmable circuits.

Additionally, there are more special hatches with various functions; please search in-game.

### Other Useful Devices and Items

* **ME Super Tank / Super Chest**

Similar to super tanks and chests, but can be directly connected to the AE network without requiring an ME Storage Bus.

* **ME Interface Overlay Plate**

Functions identically to an ME Interface but can be directly overlaid onto machines, supporting both Dual-Input and P2P versions.

* **Power Feedstock Distributor**

A great solution for automating assembly lines; see in-game tooltips for detailed usage.

* **Warning Anchor**

Connects to the AE network and warns in the chat box when the chunk containing AE cables is unloaded.

* **OC Expansion Components**

Adds some additional components for the Open Computers Mod, such as the Advanced Wireless Redstone Card for accessing GT Advanced Wireless Redstone and the API Card for providing ore dictionary access and conversion of numerical and string IDs. For more details, search in NEI.

> Explore more content in-game. If you have any text understanding questions, you can search for related video tutorials on Bilibili.

## Compatibility

Currently, this MOD is confirmed to support all content in the official GTNH version. If you find that a certain hatch does not work on a certain machine, it means that the machine itself does not support assembly, unless the original code of the machine is injected and modified or fixed by the GTNH team. However, you can still raise related issues to help identify the problem.

At the same time, support for other community MODs, such as Twist Space Technology Mod, may not be comprehensive or timely. If you encounter compatibility issues with other MODs, please submit an issue.
