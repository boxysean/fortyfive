#!/usr/bin/env node
// The only place the CLI exits: run() returns the code so it stays callable from tests.
import { run } from '../src/cli.js';

process.exit(await run(process.argv.slice(2)));
