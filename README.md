# GNC Library

A library of the core logic of Guild & Commerce
This library creates a version agnostic codebase for all GnC versions.

The goal of this library is to unify features across versions such that 
the only changes needed are UI elements or those which change with MC 
version.  By keeping the core logic of the mod, as agnostic as possible,
changes to MC's code minimally impacts version updates.

My current goals for this library are to:
- implement planned features
- backport new content to 1.12
- create versions for 1.12-1.15 after 1.12 and 1.16 are released. 